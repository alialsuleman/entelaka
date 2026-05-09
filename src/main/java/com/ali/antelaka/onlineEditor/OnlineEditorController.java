package com.ali.antelaka.onlineEditor;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/onlineeditor")
@RequiredArgsConstructor
public class OnlineEditorController {

    private static final List<Map<String, Object>> LANGUAGES = List.of(
            Map.of("id", 62,  "name", "Java (OpenJDK 8)"),
            Map.of("id", 63,  "name", "JavaScript (Node.js 14)"),
            Map.of("id", 74,  "name", "TypeScript (3.9)"),
            Map.of("id", 94,  "name", "TypeScript (4.4)"),
            Map.of("id", 101, "name", "TypeScript (5.0)"),
            Map.of("id", 70,  "name", "Python (2.7)"),
            Map.of("id", 71,  "name", "Python (3.8)"),
            Map.of("id", 50,  "name", "C (GCC 9.2.0)"),
            Map.of("id", 54,  "name", "C++ (GCC 9.2.0)"),
            Map.of("id", 76,  "name", "C++ (Clang 7.0.1)"),
            Map.of("id", 51,  "name", "C# (Mono 6.6.0)"),
            Map.of("id", 68,  "name", "PHP (7.4)")
    );

    @Autowired
    private OnlineEditorService editorService;

    // ─────────────────────────────────────────────
    //  POST /onlineeditor  →  Submit code
    // ─────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse> submitCode(
            @RequestBody Map<String, Object> payload,
            Principal connectedUser
    ) {
        User user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        String sourceCode = (String) payload.get("source_code");
        int    languageId = (Integer) payload.get("language_id");
        String input      = (String) payload.get("input");

        RunCodeResponse runCodeResponse = editorService.submitCode(user, sourceCode, languageId, input);

        Map<String, Object> body = new HashMap<>();
        body.put("numberOfRequestsPerDay", runCodeResponse.getNumberOfRequestsPerDay());
        body.put("maxRunsPerDay",          runCodeResponse.getMaxRunsPerDay());

        if (runCodeResponse.getCode() == 0) {
            body.put("message", runCodeResponse.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(true)
                            .message(runCodeResponse.getMessage())
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .data(body)
                            .build());
        }

        body.put("token", runCodeResponse.getMessage());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.builder()
                        .success(true)
                        .message("Execution started.")
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .data(body)
                        .build());
    }

    // ─────────────────────────────────────────────
    //  GET /onlineeditor/{token}  →  Poll result
    // ─────────────────────────────────────────────
    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResult(@PathVariable String token) {
        try {
            Map<String, Object> result = editorService.getResult(token);

            if (result == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("Invalid token or submission not found.")
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.NOT_FOUND.value())
                                .data(null)
                                .build());
            }

            Integer statusId = extractStatusId(result);

            if (statusId != null && (statusId == 1 || statusId == 2)) {
                return ResponseEntity
                        .status(HttpStatus.ACCEPTED)
                        .body(ApiResponse.<Map<String, Object>>builder()
                                .success(true)
                                .message("Execution is still running. Try again later.")
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.ACCEPTED.value())
                                .data(result)
                                .build());
            }

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .message("Execution result fetched.")
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.OK.value())
                            .data(result)
                            .build());

        } catch (HttpClientErrorException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("Judge0 error: " + e.getResponseBodyAsString())
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.BAD_GATEWAY.value())
                            .data(null)
                            .build());

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("Internal server error.")
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(null)
                            .build());
        }
    }

    // ─────────────────────────────────────────────
    //  GET /onlineeditor/languages  →  List languages
    // ─────────────────────────────────────────────
    @GetMapping("languages")
    public ResponseEntity<ApiResponse<?>> getLanguages() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("ok")
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .data(LANGUAGES)
                        .build());
    }

    // ─────────────────────────────────────────────
    //  Private helpers
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private Integer extractStatusId(Map<String, Object> result) {
        try {
            Map<String, Object> statusMap = (Map<String, Object>) result.get("status");
            if (statusMap == null) return null;
            Object id = statusMap.get("id");
            return (id instanceof Number) ? ((Number) id).intValue() : null;
        } catch (Exception e) {
            return null;
        }
    }
}