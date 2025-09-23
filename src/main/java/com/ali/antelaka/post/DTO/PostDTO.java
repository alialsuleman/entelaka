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
    private Integer pageId;

    private List<PostImage> postImages;


    public PostDTO(Post post) {
        this.id = post.getId();
        this.text = post.getText();
        this.tag = post.getTag();
        this.numberOfLikes = post.getNumberOfLikes();
        this.numberOfComment = post.getNumberOfComment();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.userId = post.getUser() != null ? post.getUser().getId() : null;
        this.pageId = post.getPageEntity() != null ? post.getPageEntity().getId() : null;
        this.postImages =  post.getPostImages()!= null ?  post.getPostImages() : new ArrayList();
    }

}
