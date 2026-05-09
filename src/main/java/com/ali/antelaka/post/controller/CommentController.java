// ─────────────────────────────────────────────
//  CommentController.java
// ─────────────────────────────────────────────
package com.ali.antelaka.post.controller;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.post.service.CommentService;
import com.ali.antelaka.post.DTO.CommentDTO;
import com.ali.antelaka.post.DTO.CommentHistoryDTO;
import com.ali.antelaka.post.request.CreateCommentRequest;
import com.ali.antelaka.post.request.EditCommentRequest;
import com.ali.antelaka.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ─────────────────────────────────────────────
    //  POST /posts/comment
    // ─────────────────────────────────────────────
    @PostMapping("/comment")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> addComment(
            @RequestParam Integer postId,
            @RequestParam Integer commentParent,
            @RequestParam Integer repliedUserId,
            @RequestBody CreateCommentRequest createCommentRequest,
            Principal connectedUser
    ) {
        if (commentParent == 0) commentParent = null;

        var user = extractUser(connectedUser);
        CommentDTO commentDTO = commentService.createComment(
                user, postId, commentParent, repliedUserId, createCommentRequest);

        if (commentDTO == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .success(false)
                    .message("Something went wrong")
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .data(null)
                    .build());
        }

        commentDTO.getOwner().setMe(true);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder()
                .success(true)
                .message("Comment added successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data(commentDTO)
                .build());
    }

    // ─────────────────────────────────────────────
    //  POST /posts/comment/edit
    // ─────────────────────────────────────────────
    @PostMapping("/comment/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> editComment(
            @RequestBody EditCommentRequest req,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        CommentDTO commentDTO = commentService.editComment(req.getCommentId(), req.getNewText(), user);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Comment updated successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(commentDTO)
                .build());
    }

    // ─────────────────────────────────────────────
    //  DELETE /posts/comments/{id}
    // ─────────────────────────────────────────────
    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> deleteComment(
            @PathVariable Integer id,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        boolean ok = commentService.deleteComment(id, user);

        if (!ok) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.builder()
                    .success(false)
                    .message("You are not allowed to delete this comment")
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.FORBIDDEN.value())
                    .build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Comment deleted successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build());
    }

    // ─────────────────────────────────────────────
    //  GET /posts/{postId}/comments
    // ─────────────────────────────────────────────
    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<?>> getPostComments(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Principal connectedUser
    ) {
        User user = extractUser(connectedUser);
        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending());

        List<CommentDTO> comments = commentService.getCommentsByPostIdWithUserInfo(postId, pageable, user);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Comments fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(comments)
                .build());
    }

    // ─────────────────────────────────────────────
    //  GET /posts/repliesoncomment
    // ─────────────────────────────────────────────
    @GetMapping("/repliesoncomment")
    public ResponseEntity<ApiResponse<?>> getRepliesOnComments(
            @RequestParam int commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Principal connectedUser
    ) {
        User user = extractUser(connectedUser);
        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending());

        List<CommentDTO> comments = commentService.getRepliesOnCommentsByCommentIdWithUserInfo(
                commentId, pageable, user);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Comments fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(comments)
                .build());
    }

    // ─────────────────────────────────────────────
    //  GET /posts/commenthistory
    // ─────────────────────────────────────────────
    @GetMapping("/commenthistory")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> getUserCommentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        Page<CommentHistoryDTO> history = commentService.getUserCommentHistory(user.getId(), page, size);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("User comment history fetched successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .data(history)
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