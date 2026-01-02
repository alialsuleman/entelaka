package com.ali.antelaka.onlineEditor;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OnlineEditorService {

    private final RestTemplate restTemplate;
    private final UserRunRepository userRunRepository; // قاعدة بيانات لتخزين عدد ال runs

    @Value("${onlinecompiler.max-runs-per-day}")
    private int maxRunsPerDay;



    @Value("${onlinecompiler.judge0.url}")
    private String judge0Url;

    // إرسال الكود فقط
    public RunCodeResponse submitCode(User user, String sourceCode, int languageId) {

        RunCodeResponse runCodeResponse = new RunCodeResponse() ;
        runCodeResponse.setCode(1);
        runCodeResponse.setMaxRunsPerDay(maxRunsPerDay) ;

        int numberOfRequestsPerDay = getTodayCount(user.getId());
        runCodeResponse.setNumberOfRequestsPerDay(numberOfRequestsPerDay);
        if (numberOfRequestsPerDay >= maxRunsPerDay) {
            runCodeResponse.setCode(0);
            runCodeResponse.setMessage("You have reached the limit of executions for today.") ;
            return runCodeResponse ;
        }


        // 1) حذف البيانات القديمة
        LocalDate today = LocalDate.now();
        userRunRepository.deleteByUserIdAndDateBefore(user.getId(), today);
        incrementRun(user.getId());
        runCodeResponse.setNumberOfRequestsPerDay(numberOfRequestsPerDay+1);

        Map<String, Object> body = new HashMap<>();
        body.put("source_code", sourceCode);
        body.put("language_id", languageId);

        ResponseEntity<Map> postResponse = restTemplate.postForEntity(
                judge0Url + "/submissions",
                body,
                Map.class
        );

        runCodeResponse.setMessage((String) postResponse.getBody().get("token"));
        return runCodeResponse ;
    }



    public Map<String, Object> getResult(String token) {
        try {
            ResponseEntity<Map> getResponse = restTemplate.getForEntity(
                    judge0Url + "/submissions/" + token + "?base64_encoded=true",
                    Map.class
            );

            Map<String, Object> body = getResponse.getBody();
            if (body == null) return null;

            // فك الترميز لجميع الحقول الممكنة
            decodeBase64Safe(body, "stdout");
            decodeBase64Safe(body, "stderr");
            decodeBase64Safe(body, "compile_output");

            // إنشاء final_output جاهز للفرونت إند
            Map<String, Object> statusMap = (Map<String, Object>) body.get("status");
            Integer statusId = (statusMap != null) ? (Integer) statusMap.get("id") : null;

            String finalOutput = null;
            if (statusId != null) {
                switch (statusId) {
                    case 3: // Accepted
                        finalOutput = (String) body.get("stdout");
                        break;
                    case 6: // Compilation Error
                        finalOutput = (String) body.get("compile_output");
                        break;
                    default:
                        finalOutput = (String) body.get("stderr");
                }
            }
            body.put("final_output", finalOutput);

            return body;

        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw e;
        }
    }

    private void decodeBase64Safe(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) return;
        if (!(value instanceof String encoded)) return;
        if (encoded.isBlank()) return;

        try {
            // إزالة أي أسطر جديدة أو مسافات غير مرغوب فيها قبل فك الترميز
            String cleaned = encoded.replaceAll("\\s+", "");
            byte[] decodedBytes = Base64.getDecoder().decode(cleaned);
            body.put(key, new String(decodedBytes, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            // ليست Base64 صالح → اتركها كما هي
        }
    }





    public int incrementRun(Integer userId) {

        LocalDate today = LocalDate.now();


        // 2) جلب بيانات اليوم إذا موجودة
        UserRun userRun = userRunRepository
                .findByUserIdAndDate(userId, today)
                .orElseGet(() -> UserRun.builder()
                        .userId(userId)
                        .date(today)
                        .numberOfTimePerDay(0)
                        .build()
                );

        // 3) زيادة العداد
        userRun.setNumberOfTimePerDay(userRun.getNumberOfTimePerDay() + 1);

        // 4) حفظ التعديل
        userRunRepository.save(userRun);

        return userRun.getNumberOfTimePerDay();
    }

    public int getTodayCount(Integer userId) {
        return userRunRepository
                .findByUserIdAndDate(userId, LocalDate.now())
                .map(UserRun::getNumberOfTimePerDay)
                .orElse(0);
    }








}