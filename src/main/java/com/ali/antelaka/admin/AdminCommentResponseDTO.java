package com.ali.antelaka.admin;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminCommentResponseDTO {
    private Integer id;
    private String text;
    private Integer numberOfLikes;
    private Integer numberOfSubComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer userId;
    private String userFirstname;
    private String userLastname;
    private String userEmail;
    private String userImagePath;
    private Integer postId;
    private String postText;
    private Integer commentParentId;
    private Integer repliedUserId;
    private String repliedUsername;
    private boolean userEnabled;
}