package com.ali.antelaka.post;


import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.follow.FollowRepository;
import com.ali.antelaka.post.DTO.CommentDTO;
import com.ali.antelaka.post.DTO.PostDTO;
import com.ali.antelaka.post.entity.Comment;
import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.post.repository.*;
import com.ali.antelaka.post.request.CreateCommentRequest;
import com.ali.antelaka.post.request.CreatePostRequest;
import com.ali.antelaka.post.request.EditCommentRequest;
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
    private LikeOnCommentRepository likeOnCommentRepository;

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

    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<?>> getUserPosts(
            @PathVariable("id") Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal connectedUser) {

        var currentUser = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if (userId == 0 && currentUser!=null  )
        {
            userId = currentUser.getId() ;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<PostDTO> userPosts = postService.getPostsByUser(userId, currentUser, pageable);

        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("User posts fetched successfully")
                .status(200)
                .timestamp(LocalDateTime.now())
                .data(userPosts)
                .build();

        return ResponseEntity.ok(response);
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


    @PostMapping ("/like/comment/{commentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> addLikeOnComment(
            @PathVariable Integer commentId,
            Principal connectedUser
    )
    {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        boolean ok = this.postService.flipLikeOnComment(user, commentId) ;

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

    @GetMapping("/getsavedpost")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> getSavedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal connectedUser
    ) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<PostDTO> savedPosts = postService.getSavedPosts(user, pageable);

        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("Saved posts fetched successfully")
                .status(200)
                .timestamp(LocalDateTime.now())
                .data(savedPosts)
                .build();

        return ResponseEntity.ok(response);
    }

    // need edit
    @PostMapping ("/{postId}/save/")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> savePost(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "false" ) boolean isPublic,
            Principal connectedUser
    )
    {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        int ok = this.postService.flipSave(user, postId , isPublic) ;

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



    @PostMapping ("/comment")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> addComment (

            @RequestParam() Integer postId,
            @RequestParam() Integer commentParent,
            @RequestParam() Integer  repliedUserId  ,

            @RequestBody CreateCommentRequest createCommentRequest,
            Principal connectedUser
    ) {

        if (commentParent == 0 ) commentParent= null ;

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        CommentDTO commentDTO = this.postService.createComment(user, postId , commentParent  , repliedUserId, createCommentRequest) ;

        commentDTO.getOwner().setMe(true);


        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("comment added successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (null)
                .build();


        if (commentDTO == null)
        {
            res.setMessage("Something went wrong");
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(res) ;
        }
        res.setData(commentDTO);

        return  ResponseEntity.status(HttpStatus.CREATED.value()).body(res) ;
    }

    @PostMapping("/comment/edit")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<?>> editComment(
            @RequestBody EditCommentRequest req,
            Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        Comment comment = postService.editComment(req.getCommentId(), req.getNewText(), user);

        CommentDTO commentDTO =  new CommentDTO(comment, user , likeOnCommentRepository , followRepository) ;


        ApiResponse res = ApiResponse.builder()
                .success(true)
                .message("Comment updated successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(commentDTO)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }





    @GetMapping("/search")
   // @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse> searchPosts(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir ,
            Principal connectedUser
    ) {

        User user  = null ;
        if (connectedUser != null )user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(user != null)
            System.out.println(user.getFirstname());

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        List<PostDTO> result = postService.searchPosts(tag, searchText, pageable , user);
        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Post fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (result)
                .build();

        return ResponseEntity.ok(res);
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
                .status(HttpStatus.OK.value())
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
                .status(HttpStatus.OK.value())
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
                .status(HttpStatus.OK.value())
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
                .status(HttpStatus.OK.value())
                .data (posts)
                .build();



        return ResponseEntity.ok(res);
    }


    @GetMapping("/{postId}/comments")
    //@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getPostComments(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Principal connectedUser
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
        );

        User user  = null ;
        if (connectedUser != null )user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(user != null) System.out.println(user.getFirstname());


        List<CommentDTO> comments = postService.getCommentsByPostIdWithUserInfo(postId, pageable , user);

        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Comments fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data (comments)
                .build();
        return ResponseEntity.ok(res);
    }



    @GetMapping("/repliesoncomment")
    //@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getRepliesOnComments(

            @RequestParam() int commentId ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction ,
            Principal connectedUser
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
        );


        User user  = null ;
        if (connectedUser != null )user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(user != null) System.out.println(user.getFirstname());

        List<CommentDTO> comments = postService.getRepliesOnCommentsByCommentIdWithUserInfo(commentId, pageable , user);

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

        if (comment.getCommentParent() != null)
        {
            comment.getCommentParent().setNumberOfSubComment( comment.getCommentParent().getNumberOfSubComment()-1);
            this.commentRepository.save( comment.getCommentParent()) ;
        }

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



    @GetMapping("/commenthistory")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<?>> getUserCommentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size ,
            Principal connectedUser
    ) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        var history = postService.getUserCommentHistory(user.getId(), page, size);

        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("User comment history fetched successfully")
                .status(200)
                .timestamp(LocalDateTime.now())
                .data(history)
                .build();

        return ResponseEntity.ok(response);
    }


}
