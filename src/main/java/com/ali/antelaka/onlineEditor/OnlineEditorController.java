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
//
//    @PostMapping()
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
//    public ResponseEntity run (Principal connectedUser ) {
//
//        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
//
//        ApiResponse res =  ApiResponse.builder()
//                .success(true)
//                .message("Done !")
//                .timestamp(LocalDateTime.now())
//                .status(HttpStatus.CREATED.value())
//                .data (???)
//                .build();
//        return ResponseEntity.status(HttpStatus.CREATED).body(res) ;
//
//    }
//
//
//    @GetMapping("/{token}")
//    public ResponseEntity<ApiResponse<Page<User>>> getFollowing(@PathVariable Integer token ) {
//        var data   =   ???
//        ApiResponse res =  ApiResponse.builder()
//                .success(true)
//                .message("following List :")
//                .timestamp(LocalDateTime.now())
//                .status(HttpStatus.OK.value())
//                .data (data)
//                .build();
//
//
//        return ResponseEntity.ok(res) ;
//    }


        @Autowired
        private OnlineEditorService editorService;

        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public ResponseEntity<ApiResponse> submitCode(
                @RequestBody Map<String, Object> payload ,
                Principal connectedUser
        ) {

            var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

            String sourceCode = (String) payload.get("source_code");
            int languageId = (Integer) payload.get("language_id");

            RunCodeResponse runCodeResponse = editorService.submitCode(user , sourceCode, languageId);
            Map<String, Object> body = new HashMap<>();
            body.put("numberOfRequestsPerDay", runCodeResponse.getNumberOfRequestsPerDay());
            body.put("maxRunsPerDay", runCodeResponse.getMaxRunsPerDay());

            if (runCodeResponse.getCode() == 0 )
            {
                body.put("message", runCodeResponse.getMessage());
                ApiResponse<?> res = ApiResponse.builder()
                        .success(true)
                        .message(runCodeResponse.getMessage())
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .data(body)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);

            }
            body.put("token", runCodeResponse.getMessage());
            ApiResponse<?> res = ApiResponse.builder()
                    .success(true)
                    .message("Execution started.")
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .data(body)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(res);

        }

    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResult(@PathVariable String token) {

        try {
            Map<String, Object> result = editorService.getResult(token);

            // 1️⃣ التوكين غير موجود
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.<Map<String, Object>>builder()
                                .success(false)
                                .message("Invalid token or submission not found.")
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.NOT_FOUND.value())
                                .data(null)
                                .build()
                );
            }

            // 2️⃣ قراءة status بأمان
            Integer statusId = extractStatusId(result);

            // 3️⃣ التنفيذ لم يكتمل بعد
            if (statusId != null && (statusId == 1 || statusId == 2)) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                        ApiResponse.<Map<String, Object>>builder()
                                .success(true)
                                .message("Execution is still running. Try again later.")
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.ACCEPTED.value())
                                .data(result)
                                .build()
                );
            }

            // 4️⃣ التنفيذ مكتمل
            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .message("Execution result fetched.")
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.OK.value())
                            .data(result)
                            .build()
            );

        } catch (HttpClientErrorException e) {
            // خطأ قادم من Judge0
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("Judge0 error: " + e.getResponseBodyAsString())
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.BAD_GATEWAY.value())
                            .data(null)
                            .build()
            );

        } catch (Exception e) {
            // خطأ داخلي حقيقي
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("Internal server error.")
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(null)
                            .build()
            );
        }
    }


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


    private static final List<Map<String, Object>> LANGUAGES = List.of(
            Map.of("id", 45, "name", "Assembly (NASM 2.14.02)"),
            Map.of("id", 46, "name", "Bash (5.0.0)"),
            Map.of("id", 47, "name", "Basic (FBC 1.07.1)"),
            Map.of("id", 104, "name", "C (Clang 18.1.8)"),
            Map.of("id", 110, "name", "C (Clang 19.1.7)"),
            Map.of("id", 75, "name", "C (Clang 7.0.1)"),
            Map.of("id", 76, "name", "C++ (Clang 7.0.1)"),
            Map.of("id", 103, "name", "C (GCC 14.1.0)"),
            Map.of("id", 105, "name", "C++ (GCC 14.1.0)"),
            Map.of("id", 48, "name", "C (GCC 7.4.0)"),
            Map.of("id", 52, "name", "C++ (GCC 7.4.0)"),
            Map.of("id", 49, "name", "C (GCC 8.3.0)"),
            Map.of("id", 53, "name", "C++ (GCC 8.3.0)"),
            Map.of("id", 50, "name", "C (GCC 9.2.0)"),
            Map.of("id", 54, "name", "C++ (GCC 9.2.0)"),
            Map.of("id", 86, "name", "Clojure (1.10.1)"),
            Map.of("id", 51, "name", "C# (Mono 6.6.0.161)"),
            Map.of("id", 77, "name", "COBOL (GnuCOBOL 2.2)"),
            Map.of("id", 55, "name", "Common Lisp (SBCL 2.0.0)"),
            Map.of("id", 90, "name", "Dart (2.19.2)"),
            Map.of("id", 56, "name", "D (DMD 2.089.1)"),
            Map.of("id", 57, "name", "Elixir (1.9.4)"),
            Map.of("id", 58, "name", "Erlang (OTP 22.2)"),
            Map.of("id", 44, "name", "Executable"),
            Map.of("id", 87, "name", "F# (.NET Core SDK 3.1.202)"),
            Map.of("id", 59, "name", "Fortran (GFortran 9.2.0)"),
            Map.of("id", 60, "name", "Go (1.13.5)"),
            Map.of("id", 95, "name", "Go (1.18.5)"),
            Map.of("id", 106, "name", "Go (1.22.0)"),
            Map.of("id", 107, "name", "Go (1.23.5)"),
            Map.of("id", 88, "name", "Groovy (3.0.3)"),
            Map.of("id", 61, "name", "Haskell (GHC 8.8.1)"),
            Map.of("id", 96, "name", "JavaFX (JDK 17.0.6, OpenJFX 22.0.2)"),
            Map.of("id", 91, "name", "Java (JDK 17.0.6)"),
            Map.of("id", 62, "name", "Java (OpenJDK 13.0.1)"),
            Map.of("id", 63, "name", "JavaScript (Node.js 12.14.0)"),
            Map.of("id", 93, "name", "JavaScript (Node.js 18.15.0)"),
            Map.of("id", 97, "name", "JavaScript (Node.js 20.17.0)"),
            Map.of("id", 102, "name", "JavaScript (Node.js 22.08.0)"),
            Map.of("id", 78, "name", "Kotlin (1.3.70)"),
            Map.of("id", 111, "name", "Kotlin (2.1.10)"),
            Map.of("id", 64, "name", "Lua (5.3.5)"),
            Map.of("id", 89, "name", "Multi-file program"),
            Map.of("id", 79, "name", "Objective-C (Clang 7.0.1)"),
            Map.of("id", 65, "name", "OCaml (4.09.0)"),
            Map.of("id", 66, "name", "Octave (5.1.0)"),
            Map.of("id", 67, "name", "Pascal (FPC 3.0.4)"),
            Map.of("id", 85, "name", "Perl (5.28.1)"),
            Map.of("id", 68, "name", "PHP (7.4.1)"),
            Map.of("id", 98, "name", "PHP (8.3.11)"),
            Map.of("id", 43, "name", "Plain Text"),
            Map.of("id", 69, "name", "Prolog (GNU Prolog 1.4.5)"),
            Map.of("id", 70, "name", "Python (2.7.17)"),
            Map.of("id", 92, "name", "Python (3.11.2)"),
            Map.of("id", 100, "name", "Python (3.12.5)"),
            Map.of("id", 109, "name", "Python (3.13.2)"),
            Map.of("id", 113, "name", "Python (3.14.0)"),
            Map.of("id", 71, "name", "Python (3.8.1)"),
            Map.of("id", 80, "name", "R (4.0.0)"),
            Map.of("id", 99, "name", "R (4.4.1)"),
            Map.of("id", 72, "name", "Ruby (2.7.0)"),
            Map.of("id", 73, "name", "Rust (1.40.0)"),
            Map.of("id", 108, "name", "Rust (1.85.0)"),
            Map.of("id", 81, "name", "Scala (2.13.2)"),
            Map.of("id", 112, "name", "Scala (3.4.2)"),
            Map.of("id", 82, "name", "SQL (SQLite 3.27.2)"),
            Map.of("id", 83, "name", "Swift (5.2.3)"),
            Map.of("id", 74, "name", "TypeScript (3.7.4)"),
            Map.of("id", 94, "name", "TypeScript (5.0.3)"),
            Map.of("id", 101, "name", "TypeScript (5.6.2)"),
            Map.of("id", 84, "name", "Visual Basic.Net (vbnc 0.0.0.5943)")
    );

    @GetMapping("languages")
    public ResponseEntity<?> getLanguages() {


       ApiResponse< ? > res = ApiResponse.builder()
                .success(true)
                .message("ok")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(LANGUAGES)
                .build();

        return ResponseEntity.ok(res);
    }



}
