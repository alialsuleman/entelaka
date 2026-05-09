// ─────────────────────────────────────────────
//  LikeController.java
// ─────────────────────────────────────────────
package com.ali.antelaka.post.controller;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.post.service.LikeService;
import com.ali.antelaka.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    // ─────────────────────────────────────────────
    //  POST /posts/like/{postId}
    // ─────────────────────────────────────────────
    @PostMapping("/like/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> addLike(
            @PathVariable Integer postId,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        boolean ok = likeService.flipLike(user, postId);

        if (!ok) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .success(false)
                    .message("Something went wrong")
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .data(null)
                    .build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Like flipped successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(null)
                .build());
    }

    // ─────────────────────────────────────────────
    //  POST /posts/like/comment/{commentId}
    // ─────────────────────────────────────────────
    @PostMapping("/like/comment/{commentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> addLikeOnComment(
            @PathVariable Integer commentId,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        boolean ok = likeService.flipLikeOnComment(user, commentId);

        if (!ok) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .success(false)
                    .message("Something went wrong")
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .data(null)
                    .build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Like flipped successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(null)
                .build());
    }

    // ─────────────────────────────────────────────
    //  POST /posts/{postId}/save/
    // ─────────────────────────────────────────────
    @PostMapping("/{postId}/save/")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> savePost(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "false") boolean isPublic,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        int ok = likeService.flipSave(user, postId, isPublic);

        if (ok == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .success(false)
                    .message("Something went wrong")
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .data(null)
                    .build());
        }

        String message = (ok == 1) ? "Post unsaved successfully" : "Post saved successfully";
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(null)
                .build());
    }

    // ─────────────────────────────────────────────
    //  GET /posts/getsavedpost
    // ─────────────────────────────────────────────
    @GetMapping("/getsavedpost")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> getSavedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        var pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("id").descending());

        var savedPosts = likeService.getSavedPosts(user, pageable);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Saved posts fetched successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .data(savedPosts)
                .build());
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────
    private User extractUser(Principal principal) {
        if (principal == null) return null;
        return (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    }
}