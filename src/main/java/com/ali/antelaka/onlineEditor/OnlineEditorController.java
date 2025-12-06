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

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
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

            // إذا لم يعد شيء → التوكين غير موجود
            if (result == null) {
                ApiResponse<Map<String, Object>> res = ApiResponse.<Map<String, Object>>builder()
                        .success(false)
                        .message("Invalid token or submission not found.")
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .data(null)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
            }

            // جلب status.id من النتيجة
            Map statusMap = (Map) result.get("status");
            Integer statusId = (statusMap != null) ? (Integer) statusMap.get("id") : null;

            // إذا التنفيذ لم يكتمل بعد (status 1 = In Queue, 2 = Processing)
            if (statusId != null && (statusId == 1 || statusId == 2)) {
                ApiResponse<Map<String, Object>> res = ApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Execution is still running. Try again later.")
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.ACCEPTED.value()) // 202
                        .data(result)
                        .build();

                return ResponseEntity.status(HttpStatus.ACCEPTED).body(res);
            }

            // إذا التنفيذ مكتمل → أعد النتيجة
            ApiResponse<Map<String, Object>> res = ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Execution result fetched")
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .data(result)
                    .build();

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            ApiResponse<Map<String, Object>> res = ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Error fetching result: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }


}
