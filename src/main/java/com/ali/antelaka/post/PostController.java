package com.ali.antelaka.post;


import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.follow.FollowRepository;
import com.ali.antelaka.post.DTO.CommentDTO;
import com.ali.antelaka.post.DTO.PostDTO;
import com.ali.antelaka.post.entity.Comment;
import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.post.repository.CommentRepository;
import com.ali.antelaka.post.repository.LikeRepository;
import com.ali.antelaka.post.repository.PostRepository;
import com.ali.antelaka.post.repository.SaveRepository;
import com.ali.antelaka.post.request.CreateCommentRequest;
import com.ali.antelaka.post.request.CreatePostRequest;
import com.ali.antelaka.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
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

@RequestMapping("/posts")
@RestController
public class PostController {


    @Autowired
    private PostService  postService ;

    @Autowired
    private PostRepository postRepository ;

    @Autowired
    private LikeRepository likeRepository ;
    @Autowired
    private SaveRepository saveRepository ;

    @Autowired
    private FollowRepository followRepository ;


    @Autowired
    private CommentRepository commentRepository ;
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse> getPostById(
            @PathVariable Integer postId
    )
    {


        var post = this.postService.getPostById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostDTO postDTO = new PostDTO(post ,  null ,null , null , null );

        Map<String, Object> data = new HashMap<>();
        data.put("post", postDTO);

        ApiResponse res = ApiResponse.builder()
                .success(true)
                .message("Post fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(data)
                .build();

        return ResponseEntity.ok(res);
    }



    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<PostDTO>> createPost(
             @RequestBody CreatePostRequest createPostRequest ,
            Principal connectedUser
    )
    {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        Integer pageId = user.getId() ;
        System.out.println(user.toString());
        System.out.println("any");
        Post createdPost = this.postService.createPost(user, pageId , createPostRequest);
        PostDTO postDTO =  new PostDTO(createdPost , user , likeRepository , saveRepository , followRepository ) ;
        Map m =  new HashMap( ) ;
        m.put("newPost" , postDTO) ;
        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Post created successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (m)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(res) ;
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> deletePost (
            @PathVariable Integer postId,
            Principal connectedUser
    )
    {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        boolean ok = this.postService.deletePost(user, postId ) ;

        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("post deleted successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (null)
                .build();


        if (!ok)
        {
            res.setMessage("You do not have permission to delete this post.");
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(res) ;
        }


        return  ResponseEntity.status(HttpStatus.CREATED.value()).body(res) ;
    }

    @PutMapping("/update/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse> updatePost(
            @PathVariable Integer postId,
            @RequestBody CreatePostRequest updateRequest,
            Principal connectedUser
    )
    {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        Post updatedPost = this.postService.updatePost(user, postId, updateRequest);
        PostDTO postDTO = new PostDTO(updatedPost  , user , likeRepository , saveRepository , followRepository);

        Map<String, Object> data = new HashMap<>();
        data.put("updatedPost", postDTO);

        ApiResponse res = ApiResponse.builder()
                .success(true)
                .message("Post updated successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(data)
                .build();

        return ResponseEntity.ok(res);
    }






    @PostMapping ("/like/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> addLike(
            @PathVariable Integer postId,
            Principal connectedUser
    )
    {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        boolean ok = this.postService.flipLike(user, postId) ;

        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Like flipped successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (null)
                .build();


        if (!ok)
        {
            res.setMessage("Something went wrong");
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(res) ;
        }


        return  ResponseEntity.status(HttpStatus.CREATED.value()).body(res) ;
    }


    @PostMapping ("/save/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> savePost(
            @PathVariable Integer postId,
            Principal connectedUser
    )
    {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        int ok = this.postService.flipSave(user, postId) ;

        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("post saved successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (null)
                .build();

        if (ok ==  1 ) res.setMessage("post unsaved successfully");
        if ( ok ==0 )
        {
            res.setMessage("Something went wrong");
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(res) ;
        }
        return  ResponseEntity.status(HttpStatus.CREATED.value()).body(res) ;
    }



    @PostMapping ("/comment/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> addComment (
            @PathVariable Integer postId,
            @RequestBody CreateCommentRequest createCommentRequest,
            Principal connectedUser
    ) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        boolean ok = this.postService.createComment(user, postId , createCommentRequest) ;

        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("comment added successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (null)
                .build();


        if (!ok)
        {
            res.setMessage("Something went wrong");
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(res) ;
        }


        return  ResponseEntity.status(HttpStatus.CREATED.value()).body(res) ;
    }


    @GetMapping("/older")
    public ResponseEntity<ApiResponse> getOlderPosts(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "10") int limit ,
            @RequestParam(required = false) String tag,
            Principal connectedUser )
    {


        User user  = null ;
        if (connectedUser != null )user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(user != null)
        System.out.println(user.getFirstname());
        boolean onlyPublic = true ;
        List<PostDTO> posts = postService.getOlderPosts(userId, date, limit, onlyPublic , user , tag);

        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Post fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (posts)
                .build();

        return ResponseEntity.ok(res);
    }


    @GetMapping("/newer")
    public ResponseEntity<ApiResponse> getNewerPosts(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "10") int limit  ,
            @RequestParam(required = false) String tag,
            Principal connectedUser )
    {
        User user  = null ;
        if (connectedUser != null )user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(user != null) System.out.println(user.getFirstname());

        boolean onlyPublic = true ;
        List<PostDTO> posts = postService.getNewerPosts(userId, date, limit, onlyPublic , user  ,tag);

        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Post fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (posts)
                .build();

        return ResponseEntity.ok(res);
    }


    @GetMapping("/myolder")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse> getMYOlderPosts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String tag,
            Principal connectedUser
    )
    {
        //return  this.postRepository.findAllFollowingsPosts()
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        boolean onlyPublic = false ;
        List<PostDTO> posts = postService.getOlderPosts(user.getId(), date, limit, onlyPublic , user  , tag);


        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Post fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (posts)
                .build();


        return ResponseEntity.ok(res);
    }


    @GetMapping("/mynewer")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse> getMyNewerPosts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String tag,
            Principal connectedUser
    )
    {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        boolean onlyPublic = false ;
        List<PostDTO> posts = postService.getNewerPosts(user.getId(), date, limit, onlyPublic , user , tag);


        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Post fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (posts)
                .build();



        return ResponseEntity.ok(res);
    }


    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getPostComments(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
        );

        Page<CommentDTO> comments = postService.getCommentsByPostIdWithUserInfo(postId, pageable);

        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Comments fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data (comments)
                .build();
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> deleteComment(
            @PathVariable Integer id,
            Principal connectedUser
    ) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));


        if ( !comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("You are not allowed to delete this comment")
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.FORBIDDEN.value())
                            .build());
        }

        commentRepository.delete(comment);

        ApiResponse res = ApiResponse.builder()
                .success(true)
                .message("Comment deleted successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(res);
    }







}
