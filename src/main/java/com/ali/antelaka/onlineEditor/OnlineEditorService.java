package com.ali.antelaka.onlineEditor;

import com.ali.antelaka.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OnlineEditorService {

    private final RestTemplate restTemplate;
    private final UserRunRepository userRunRepository;

    @Value("${onlinecompiler.max-runs-per-day}")
    private int maxRunsPerDay;

    @Value("${onlinecompiler.judge0.url}")
    private String judge0Url;

    // ─────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────

    public RunCodeResponse submitCode(User user, String sourceCode, int languageId, String stdin) {

        int todayCount = getTodayCount(user.getId());

        RunCodeResponse response = RunCodeResponse.builder()
                .Code(1)
                .maxRunsPerDay(maxRunsPerDay)
                .numberOfRequestsPerDay(todayCount)
                .build();

        if (todayCount >= maxRunsPerDay) {
            response.setCode(0);
            response.setMessage("You have reached the limit of executions for today.");
            return response;
        }


        userRunRepository.deleteByUserIdAndDateBefore(user.getId(), LocalDate.now());
        incrementRun(user.getId());
        response.setNumberOfRequestsPerDay(todayCount + 1);


        Map<String, Object> body = new HashMap<>();
        body.put("source_code", sourceCode);
        body.put("language_id", languageId);
        if (stdin != null && !stdin.isEmpty()) {
            body.put("stdin", stdin);
        }

        ResponseEntity<Map> postResponse = restTemplate.postForEntity(
                judge0Url + "/submissions",
                body,
                Map.class
        );

        response.setMessage((String) postResponse.getBody().get("token"));
        return response;
    }

    public Map<String, Object> getResult(String token) {
        try {
            ResponseEntity<Map> getResponse = restTemplate.getForEntity(
                    judge0Url + "/submissions/" + token + "?base64_encoded=true",
                    Map.class
            );

            Map<String, Object> body = getResponse.getBody();
            if (body == null) return null;


            decodeBase64Safe(body, "stdout");
            decodeBase64Safe(body, "stderr");
            decodeBase64Safe(body, "compile_output");

             body.put("final_output", resolveFinalOutput(body));

            return body;

        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public int getTodayCount(Integer userId) {
        return userRunRepository
                .findByUserIdAndDate(userId, LocalDate.now())
                .map(UserRun::getNumberOfTimePerDay)
                .orElse(0);
    }

    // ─────────────────────────────────────────────
    //  Private helpers
    // ─────────────────────────────────────────────

    public int incrementRun(Integer userId) {
        LocalDate today = LocalDate.now();

        UserRun userRun = userRunRepository
                .findByUserIdAndDate(userId, today)
                .orElseGet(() -> UserRun.builder()
                        .userId(userId)
                        .date(today)
                        .numberOfTimePerDay(0)
                        .build());

        userRun.setNumberOfTimePerDay(userRun.getNumberOfTimePerDay() + 1);
        userRunRepository.save(userRun);
        return userRun.getNumberOfTimePerDay();
    }

    private String resolveFinalOutput(Map<String, Object> body) {
        Map<String, Object> statusMap = (Map<String, Object>) body.get("status");
        if (statusMap == null) return null;

        Integer statusId = (Integer) statusMap.get("id");
        if (statusId == null) return null;

        return switch (statusId) {
            case 3  -> (String) body.get("stdout");          // Accepted
            case 6  -> (String) body.get("compile_output");  // Compilation Error
            default -> (String) body.get("stderr");
        };
    }

    private void decodeBase64Safe(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (!(value instanceof String encoded) || encoded.isBlank()) return;

        try {
            String cleaned = encoded.replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(cleaned);
            body.put(key, new String(decoded, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException ignored) {
        }
    }
}