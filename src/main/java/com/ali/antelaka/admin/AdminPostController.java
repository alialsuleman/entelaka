package com.ali.antelaka.admin;


import com.ali.antelaka.admin.*;
import com.ali.antelaka.admin.AdminPostService;
import com.ali.antelaka.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminPostController {

    private final AdminPostService adminPostService;

    // ==================== POSTS ENDPOINTS ====================

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<AdminPostResponseDTO>>> getAllPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        try {
            AdminPostSearchRequest request = AdminPostSearchRequest.builder()
                    .keyword(keyword)
                    .tag(tag)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();

            Page<AdminPostResponseDTO> posts = adminPostService.getAllPosts(request);

            ApiResponse<Page<AdminPostResponseDTO>> response = ApiResponse
                    .<Page<AdminPostResponseDTO>>builder()
                    .success(true)
                    .message("Posts retrieved successfully")
                    .data(posts)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Error retrieving posts: " + e.getMessage());
        }
    }

    @GetMapping("/posts/search")
    public ResponseEntity<ApiResponse<Page<AdminPostResponseDTO>>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            AdminPostSearchRequest request = AdminPostSearchRequest.builder()
                    .keyword(keyword)
                    .page(page)
                    .size(size)
                    .build();

            Page<AdminPostResponseDTO> posts = adminPostService.getAllPosts(request);

            ApiResponse<Page<AdminPostResponseDTO>> response = ApiResponse
                    .<Page<AdminPostResponseDTO>>builder()
                    .success(true)
                    .message("Posts searched successfully")
                    .data(posts)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Error searching posts: " + e.getMessage());
        }
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<AdminPostResponseDTO>> getPostDetails(@PathVariable Integer postId) {
        try {
            AdminPostResponseDTO post = adminPostService.getPostDetails(postId);

            ApiResponse<AdminPostResponseDTO> response = ApiResponse
                    .<AdminPostResponseDTO>builder()
                    .success(true)
                    .message("Post details retrieved successfully")
                    .data(post)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return buildErrorResponse("Error retrieving post details: " + e.getMessage());
        }
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Integer postId) {
        try {
            adminPostService.deletePost(postId);

            ApiResponse<Void> response = ApiResponse
                    .<Void>builder()
                    .success(true)
                    .message("Post deleted successfully")
                    .data(null)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return buildErrorResponse("Error deleting post: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{userId}/posts")
    public ResponseEntity<ApiResponse<Void>> deleteAllUserPosts(@PathVariable Integer userId) {
        try {
            adminPostService.deleteAllUserPosts(userId);

            ApiResponse<Void> response = ApiResponse
                    .<Void>builder()
                    .success(true)
                    .message("All user posts deleted successfully")
                    .data(null)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return buildErrorResponse("Error deleting user posts: " + e.getMessage());
        }
    }








    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Integer commentId) {
        try {
            adminPostService.deleteComment(commentId);

            ApiResponse<Void> response = ApiResponse
                    .<Void>builder()
                    .success(true)
                    .message("Comment deleted successfully")
                    .data(null)
                    .errors(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return buildErrorResponse("Error deleting comment: " + e.getMessage());
        }
    }


    // ==================== HELPER METHODS ====================

    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(String message) {
        return buildErrorResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(String message, HttpStatus status) {
        ApiResponse<T> response = ApiResponse
                .<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(Collections.singletonList(message))
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}