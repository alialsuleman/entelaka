package com.ali.antelaka.post.DTO;

import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.post.entity.PostImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private Integer userId;
    private String username ;

    private String userImagePath ;
    private Integer pageId;

    private List<PostImage> postImages;


    public PostDTO(Post post) {
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
        this.userId = post.getUser() != null ? post.getUser().getId() : null;
        this.username = name!= null ?name  : null ;
        this.userImagePath =  post.getUser().getImagePath() != null ?post.getUser().getImagePath()  : null ;
        this.pageId = post.getPageEntity() != null ? post.getPageEntity().getId() : null;
        this.postImages =  post.getPostImages()!= null ?  post.getPostImages() : new ArrayList();
    }

}
