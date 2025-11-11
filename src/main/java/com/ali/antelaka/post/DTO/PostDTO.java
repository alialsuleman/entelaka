package com.ali.antelaka.post.DTO;

import com.ali.antelaka.follow.Follow;
import com.ali.antelaka.follow.FollowRepository;
import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.post.entity.PostImage;
import com.ali.antelaka.post.repository.LikeRepository;
import com.ali.antelaka.post.repository.SaveRepository;
import com.ali.antelaka.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDTO {



    private Integer id;
    private String text;
    private String tag;

    private Integer numberOfLikes;
    private Integer numberOfComment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Owner owner ;


    private Integer pageId;

    private List<PostImage> postImages;
    private boolean isLiked ;
    private boolean isSaved ;
    public PostDTO(Post post ) {
        this.owner = new Owner() ;

        String name = "";
        if ( post.getUser().getFirstname()  != null )
        {
            name+= post.getUser().getFirstname() ;
        }
        if ( post.getUser().getLastname()!=  null)
        {
            name += " " +  post.getUser().getLastname() ;
        }
        this.id = post.getId();
        this.text = post.getText();
        this.tag = post.getTag();
        this.numberOfLikes = post.getNumberOfLikes();
        this.numberOfComment = post.getNumberOfComment();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        this.owner.setUserId(post.getUser() != null ? post.getUser().getId() : null ) ;
        this.owner.setUsername( name!= null ?name  : null );
        this.owner.setUserImagePath( post.getUser().getImagePath() != null ?post.getUser().getImagePath()  : null ) ;


        this.pageId = post.getPageEntity() != null ? post.getPageEntity().getId() : null;
        this.postImages =  post.getPostImages()!= null ?  post.getPostImages() : new ArrayList();
        this.isLiked = false ;
        this.isSaved = false ;

    }

    public PostDTO(Post post , User user, LikeRepository likeRepository , SaveRepository saveRepository , FollowRepository followRepository) {
        String name = "";
        if ( post.getUser().getFirstname()  != null )
        {
            name+= post.getUser().getFirstname() ;
        }
        if ( post.getUser().getLastname()!=  null)
        {
            name += " " +  post.getUser().getLastname() ;
        }
        this.id = post.getId();
        this.text = post.getText();
        this.tag = post.getTag();
        this.numberOfLikes = post.getNumberOfLikes();
        this.numberOfComment = post.getNumberOfComment();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        this.owner= new Owner() ;
        this.owner.setUserId(post.getUser() != null ? post.getUser().getId() : null ) ;
        this.owner.setUsername( name!= null ?name  : null );
        this.owner.setUserImagePath( post.getUser().getImagePath() != null ?post.getUser().getImagePath()  : null ) ;


        this.pageId = post.getPageEntity() != null ? post.getPageEntity().getId() : null;
        this.postImages =  post.getPostImages()!= null ?  post.getPostImages() : new ArrayList();
        this.isLiked = false ;
        this.isSaved = false ;

        if (user != null )
        {
            var is_added  = likeRepository.findByUserIdAndPostId(user.getId() , post.getId()) ;
            if (is_added.isPresent()) {
                this.isLiked = true ;
            }
            var is_saved  = saveRepository.findByUserIdAndPostId(user.getId() , post.getId()) ;
            if (is_saved.isPresent()) {
                this.isSaved = true ;
            }

            if (user.getId() == post.getUser().getId()) this.owner.setMe(true);
            else this.owner.setMe(false);

            boolean isFollowing = followRepository.findByFollowerAndFollowing(user, post.getUser())
                    .isPresent();
            this.owner.setIfollowingHim(isFollowing);

        }



    }

}
