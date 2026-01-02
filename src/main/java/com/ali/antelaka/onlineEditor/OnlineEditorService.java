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

            if (getResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }

            Map<String, Object> body = getResponse.getBody();
            if (body == null) return null;

            // قائمة الحقول التي نريد فك ترميزها
            String[] keysToDecode = {"stdout", "stderr", "compile_output"};

            for (String key : keysToDecode) {
                decodeBase64Safe(body, key);
            }

            return body;

        } catch (HttpClientErrorException.NotFound e) {
            return null; // التوكين غير صحيح
        } catch (Exception e) {
            throw e; // أي خطأ آخر يتم تمريره إلى الـ controller
        }
    }

    /**
     * يحاول فك ترميز Base64 للحقل إذا كان موجودًا وصالحًا.
     */
    private void decodeBase64Safe(Map<String, Object> body, String key) {
        Object value = body.get(key);

        // تجاهل الحقول غير الموجودة أو null
        if (value == null) return;

        // إذا الحقل ليس نص → تجاهل
        if (!(value instanceof String encoded)) return;

        // تجاهل النصوص الفارغة أو تحتوي فقط على مسافات
        if (encoded.isBlank()) return;

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encoded.trim());
            body.put(key, new String(decodedBytes, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            // النص ليس Base64 صالح → اتركه كما هو
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