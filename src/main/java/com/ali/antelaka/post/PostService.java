package com.ali.antelaka.post;

import com.ali.antelaka.follow.FollowRepository;
import com.ali.antelaka.page.PageRepository;
import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.post.DTO.CommentDTO;
import com.ali.antelaka.post.DTO.PostDTO;
import com.ali.antelaka.post.entity.*;
import com.ali.antelaka.post.repository.*;
import com.ali.antelaka.post.request.CreateCommentRequest;
import com.ali.antelaka.post.request.CreatePostRequest;
import com.ali.antelaka.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {


    @Autowired
    private PostRepository postRepository ;
    @Autowired
    private PageRepository pageRepository ;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private LikeRepository likeRepository ;

    @Autowired
    private CommentRepository commentRepository ;

    @Autowired
    private SaveRepository saveRepository ;

    @Autowired
    private FollowRepository followRepository ;

    public Optional<Post> getPostById(Integer postId)
    {
        //
        return this.postRepository.findById(postId);
    }


    public Post  createPost (User user , Integer pageId  , CreatePostRequest createPostRequest )
    {
        PageEntity pageEntity =  this.pageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Page not exist"));



        Post post =  Post.builder()

                .tag(createPostRequest.getTag())
                .text(createPostRequest.getText())
                .user(user)
                .pageEntity(pageEntity)
                .build();
        var n_post = this.postRepository.save(post) ;
        List postImgs = new ArrayList<PostImage>( ) ;

        if (createPostRequest.getPostImageIds()  != null) {
            createPostRequest.getPostImageIds().forEach(id -> {
                //System.out.println(id);
                var img = this.postImageRepository.findById(id).get();
                if (img.getPost() !=  null) throw new RuntimeException("unvalid image selected") ;
                img.setPost(n_post) ;
                postImgs.add(img) ;
                this.postImageRepository.save(img) ;
            });
            n_post.setPostImages(postImgs);
        }


        var savedPost = this.postRepository.save(n_post) ;

        return savedPost  ;
    }


    public boolean flipLike  (User user,Integer postId  )
    {
        var o_post =  this.postRepository.findById(postId) ;
        if (!o_post.isPresent()) return false ;

        var is_added  =  this.likeRepository.findByUserIdAndPostId(user.getId() , postId) ;
        var post= o_post.get();

        if (is_added.isPresent())
        {
            this.likeRepository.delete(is_added.get());
            post.setNumberOfLikes(post.getNumberOfLikes() -1  ) ;
            this.postRepository.save(post) ;
        }else {
            var like = LikeEntity.builder()
                    .post(post)
                    .user(user)
                    .build() ;
            this.likeRepository.save(like) ;
            post.setNumberOfLikes(post.getNumberOfLikes()   + 1  ) ;
            this.postRepository.save(post) ;
        }
        return true ;
    }


    public int flipSave  (User user,Integer postId , boolean isPublic )
    {
        var o_post =  this.postRepository.findById(postId) ;
        if (!o_post.isPresent()) return 0 ;

        var is_added  =  this.saveRepository.findByUserIdAndPostId(user.getId() , postId) ;
        var post= o_post.get();

        if (is_added.isPresent())
        {
            this.saveRepository.delete(is_added.get());
            return 1 ;

        }else {
            var saveEntity = SaveEntity.builder()
                    .post(post)
                    .user(user)
                    .isPublic(isPublic)
                    .build() ;
            this.saveRepository.save(saveEntity) ;
         }
        return 2 ;
    }

    public boolean createComment  (User user, Integer postId, Integer commentParent , CreateCommentRequest  createCommentRequest)
    {
        var o_post =  this.postRepository.findById(postId) ;
        if (!o_post.isPresent()) return false ;

        System.out.println(commentParent);
        Comment parentComment  = null ;
        int inc =0 ;
        if (commentParent != null )
        {
            var o_comment =  this.commentRepository.findById(commentParent) ;
            if (!o_comment.isPresent()) return false ;

            parentComment = o_comment.get() ;
            parentComment.setNumberOfSubComment(parentComment.getNumberOfSubComment()+1) ;
            System.out.println("ggood");

            this.commentRepository.save(parentComment) ;

            inc =0 ;
        }

        var post= o_post.get();
        var comment = Comment.builder()
                .post(post)
                .user(user)
                .commentParent(parentComment)
                .text(createCommentRequest.getText())
                .build() ;



        this.commentRepository.save(comment) ;

        post.setNumberOfComment(post.getNumberOfComment() + 1  ); ;
        this.postRepository.save(post) ;

        return true ;
    }

    public boolean deletePost  (User user, Integer postId )
    {
        var o_post =  this.postRepository.findById(postId) ;
        if (!o_post.isPresent()) return false ;

        var post= o_post.get();
        if (! post.getUser().getId().equals( user.getId()) )
        {
            return false ;
        }
        this.postRepository.delete(post) ;

        return true ;
    }


    public Post updatePost(User user, Integer postId, CreatePostRequest updateRequest)
    {
        var o_post = this.postRepository.findById(postId);
        if (!o_post.isPresent()) {
            throw new RuntimeException("Post not found");
        }

        var post = o_post.get();

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to update this post.");
        }


        if (updateRequest.getText() != null )
            post.setText(updateRequest.getText());

        if (updateRequest.getTag() != null )
            post.setTag(updateRequest.getTag());

        post.setUpdatedAt(LocalDateTime.now());


        if (updateRequest.getPostImageIds() != null) {
            List<PostImage> newImages = new ArrayList<>();
            for (Integer id : updateRequest.getPostImageIds()) {
                var img = postImageRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Image not found"));

                if (img.getPost() != null && !img.getPost().getId().equals(post.getId())) {
                    throw new RuntimeException("Image already used in another post");
                }

                img.setPost(post);
                newImages.add(img);
            }
            post.getPostImages().clear();
            post.getPostImages().addAll(newImages);
        }

        return this.postRepository.save(post);
    }


    public List<PostDTO> searchPosts( String tag , String searchText, Pageable pageable , User user) {
        List<Post> posts =  postRepository.searchPosts( tag , searchText, pageable);
        return posts.stream()
                .map(post -> new PostDTO(post, user, likeRepository , saveRepository , followRepository ))
                .toList();

    }

    public List<PostDTO> getOlderPosts(Integer userId, LocalDateTime x, int limit  , boolean onlyPublic , User user  , String tag) {
        Pageable pageable = PageRequest.of(0, limit);
        if (x == null) {
            x = LocalDateTime.now();
        }

        List<Post> posts;
        if (onlyPublic) {
            System.out.println("get old public post");
            posts= postRepository.findOlderPublicPosts(x,tag, pageable);
        }
        else {
            System.out.println("get old not public post");
            posts =   postRepository.findOlderPosts(userId, x,tag , pageable );
        }
        return posts.stream()
                .map(post -> new PostDTO(post, user, likeRepository , saveRepository , followRepository ))
                .toList();
    }

    public List<PostDTO> getNewerPosts(Integer userId, LocalDateTime x, int limit, boolean onlyPublic , User  user  , String tag) {
        Pageable pageable = PageRequest.of(0, limit);
        if (x == null) {
            x = LocalDateTime.now();
        }

        List<Post> posts;

        if (onlyPublic) {
            posts = postRepository.findNewerPublicPosts(x, tag,  pageable );
        }
        else  {
            posts = postRepository.findNewerPosts(userId, x, tag , pageable);
        }
        return posts.stream()
                .map(post -> new PostDTO(post, user, likeRepository , saveRepository  , followRepository))
                .toList();

    }

    public Page<CommentDTO> getCommentsByPostIdWithUserInfo(Integer postId, Pageable pageable) {
        return commentRepository.findByPostIdAndCommentParentIsNullOrderByCreatedAtDesc(postId, pageable)
                .map(comment -> CommentDTO.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .createdAt(comment.getCreatedAt())
                        .updatedAt(comment.getUpdatedAt())
                        .userName(comment.getUser().getFirstname() + comment.getUser().getLastname())
                        .userId(comment.getUser().getId())
                        .userAvatar(comment.getUser().getImagePath())
                        .numberOfReplies(comment.getNumberOfSubComment())
                        .build());
    }

    public Page<CommentDTO> getRepliesOnCommentsByCommentIdWithUserInfo(Integer postId, Pageable pageable) {
        return commentRepository.findByCommentParentIdOrderByCreatedAtAsc(postId, pageable)
                .map(comment -> CommentDTO.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .createdAt(comment.getCreatedAt())
                        .updatedAt(comment.getUpdatedAt())
                        .userName(comment.getUser().getFirstname() + comment.getUser().getLastname())
                        .userId(comment.getUser().getId())
                        .userAvatar(comment.getUser().getImagePath())
                        .numberOfReplies(0)
                        .build());
    }






    public Comment editComment(Integer commentId, String newText, User user) {


        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));


        if (!comment.getUser().getId().equals(user.getId()) &&
                !user.getRole().name().equals("ADMIN")) {

            throw new RuntimeException("You are not allowed to edit this comment");
        }


        comment.setText(newText);

        return commentRepository.save(comment);
    }


}


