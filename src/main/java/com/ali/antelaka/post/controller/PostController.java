// ─────────────────────────────────────────────
//  PostController.java
// ─────────────────────────────────────────────
package com.ali.antelaka.post.controller;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.post.DTO.PostDTO;
import com.ali.antelaka.post.service.PostService;
import com.ali.antelaka.post.request.CreatePostRequest;
import com.ali.antelaka.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ─────────────────────────────────────────────
    //  GET /posts/{postId}
    // ─────────────────────────────────────────────
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<?>> getPostById(@PathVariable Integer postId) {
        var post = postService.getPostById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostDTO postDTO = new PostDTO(post, null, null, null, null);

        Map<String, Object> data = new HashMap<>();
        data.put("post", postDTO);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Post fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(data)
                .build());
    }

    // ─────────────────────────────────────────────
    //  GET /posts/user/{id}
    // ─────────────────────────────────────────────
    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<?>> getUserPosts(
            @PathVariable("id") Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal connectedUser
    ) {
        var currentUser = extractUser(connectedUser);
        if (userId == 0 && currentUser != null) {
            userId = currentUser.getId();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostDTO> userPosts = postService.getPostsByUser(userId, currentUser, pageable);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("User posts fetched successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .data(userPosts)
                .build());
    }

    // ─────────────────────────────────────────────
    //  POST /posts/create
    // ─────────────────────────────────────────────
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> createPost(
            @RequestBody CreatePostRequest createPostRequest,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        PostDTO postDTO = new PostDTO(postService.createPost(user, user.getId(), createPostRequest)) ;

        Map<String, Object> data = new HashMap<>();
        data.put("newPost", postDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder()
                .success(true)
                .message("Post created successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data(data)
                .build());
    }

    // ─────────────────────────────────────────────
    //  DELETE /posts/{postId}
    // ─────────────────────────────────────────────
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> deletePost(
            @PathVariable Integer postId,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        boolean ok = postService.deletePost(user, postId);

        if (!ok) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.builder()
                    .success(false)
                    .message("You do not have permission to delete this post.")
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.FORBIDDEN.value())
                    .data(null)
                    .build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Post deleted successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(null)
                .build());
    }

    // ─────────────────────────────────────────────
    //  PUT /posts/update/{postId}
    // ─────────────────────────────────────────────
    @PutMapping("/update/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> updatePost(
            @PathVariable Integer postId,
            @RequestBody CreatePostRequest updateRequest,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        PostDTO postDTO = new PostDTO(postService.updatePost(user, postId, updateRequest));

        Map<String, Object> data = new HashMap<>();
        data.put("updatedPost", postDTO);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Post updated successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(data)
                .build());
    }

    // ─────────────────────────────────────────────
    //  GET /posts/search
    // ─────────────────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchPosts(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Principal connectedUser
    ) {
        User user = extractUser(connectedUser);
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending());

        List<PostDTO> result = postService.searchPosts(tag, searchText, pageable, user);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Post fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(result)
                .build());
    }

    // ─────────────────────────────────────────────
    //  GET /posts/older  &  /posts/newer
    // ─────────────────────────────────────────────
    @GetMapping("/older")
    public ResponseEntity<ApiResponse<?>> getOlderPosts(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String tag,
            Principal connectedUser
    ) {
        User user = extractUser(connectedUser);
        List<PostDTO> posts = postService.getOlderPosts(userId, date, limit, true, user, tag);

        return ResponseEntity.ok(buildFetchResponse(posts));
    }

    @GetMapping("/newer")
    public ResponseEntity<ApiResponse<?>> getNewerPosts(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String tag,
            Principal connectedUser
    ) {
        User user = extractUser(connectedUser);
        List<PostDTO> posts = postService.getNewerPosts(userId, date, limit, true, user, tag);

        return ResponseEntity.ok(buildFetchResponse(posts));
    }

    // ─────────────────────────────────────────────
    //  GET /posts/myolder  &  /posts/mynewer
    // ─────────────────────────────────────────────
    @GetMapping("/myolder")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> getMyOlderPosts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String tag,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        List<PostDTO> posts = postService.getOlderPosts(user.getId(), date, limit, false, user, tag);

        return ResponseEntity.ok(buildFetchResponse(posts));
    }

    @GetMapping("/mynewer")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> getMyNewerPosts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String tag,
            Principal connectedUser
    ) {
        var user = extractUser(connectedUser);
        List<PostDTO> posts = postService.getNewerPosts(user.getId(), date, limit, false, user, tag);

        return ResponseEntity.ok(buildFetchResponse(posts));
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────
    private User extractUser(Principal principal) {
        if (principal == null) return null;
        return (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    }

    private ApiResponse<?> buildFetchResponse(Object data) {
        return ApiResponse.builder()
                .success(true)
                .message("Post fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(data)
                .build();
    }
}