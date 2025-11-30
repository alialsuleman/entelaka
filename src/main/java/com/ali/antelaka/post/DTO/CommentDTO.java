package com.ali.antelaka.post.DTO;


import com.ali.antelaka.follow.FollowRepository;
import com.ali.antelaka.post.entity.Comment;
import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.post.repository.LikeOnCommentRepository;
import com.ali.antelaka.post.repository.LikeRepository;
import com.ali.antelaka.post.repository.SaveRepository;
import com.ali.antelaka.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDTO {
    private Integer id;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer numberOfReplies ;
    private Integer numberOfLikes ;

    @Builder.Default()
    private Integer repliedUserId  =0 ;


    @Builder.Default()
    private Integer commentParentId  =0 ;

    @Builder.Default()
    private String repliedUsername = "";



    private boolean isLiked ;
    private Owner owner ;



    public CommentDTO(Comment comment ) {
        this.owner = new Owner() ;

        String name = "";
        if ( comment.getUser().getFirstname()  != null )
        {
            name+= comment.getUser().getFirstname() ;
        }
        if ( comment.getUser().getLastname()!=  null)
        {
            name += " " +  comment.getUser().getLastname() ;
        }
        this.id = comment.getId();
        this.text = comment.getText();

        this.numberOfLikes = comment.getNumberOfLikes();
        this.numberOfReplies = comment.getNumberOfSubComment();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();

        this.commentParentId =  comment.getCommentParent() != null ? comment.getCommentParent().getId() :0;

        this.repliedUsername = comment.getRepliedUsername() ;
        this.repliedUserId =  comment.getRepliedUserId() ;

        this.owner.setUserId(comment.getUser() != null ? comment.getUser().getId() : null ) ;
        this.owner.setUsername( name!= null ?name  : null );
        this.owner.setUserImagePath( comment.getUser().getImagePath() != null ?comment.getUser().getImagePath()  : null ) ;

        this.isLiked = false ;

    }

    public CommentDTO (Comment comment , User user, LikeOnCommentRepository likeOnCommentRepository  , FollowRepository followRepository) {
        String name = "";
        if (comment.getUser().getFirstname() != null) {
            name += comment.getUser().getFirstname();
        }
        if (comment.getUser().getLastname() != null) {
            name += " " + comment.getUser().getLastname();
        }
        this.id = comment.getId();
        this.text = comment.getText();
        this.numberOfLikes = comment.getNumberOfLikes();
        this.numberOfReplies = comment.getNumberOfSubComment();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();


        this.commentParentId =  comment.getCommentParent() != null ? comment.getCommentParent().getId() :0;


        this.repliedUsername = comment.getRepliedUsername() ;
        this.repliedUserId =  comment.getRepliedUserId() ;


        this.owner = new Owner();
        this.owner.setUserId(comment.getUser() != null ? comment.getUser().getId() : null);
        this.owner.setUsername(name != null ? name : null);
        this.owner.setUserImagePath(comment.getUser().getImagePath() != null ? comment.getUser().getImagePath() : null);


        this.isLiked = false;

        if (user != null) {
            var is_added = likeOnCommentRepository.findByUserIdAndCommentId(user.getId(), comment.getId());
            if (is_added.isPresent()) {
                this.isLiked = true;
            }


            if (user.getId() == comment.getUser().getId()) this.owner.setMe(true);
            else this.owner.setMe(false);

            boolean isFollowing = followRepository.findByFollowerAndFollowing(user, comment.getUser())
                    .isPresent();
            this.owner.setIfollowingHim(isFollowing);

        }
    }
}