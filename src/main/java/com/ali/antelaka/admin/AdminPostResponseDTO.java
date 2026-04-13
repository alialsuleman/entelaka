package com.ali.antelaka.admin;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminPostResponseDTO {
    private Integer id;
    private String text;
    private String tag;
    private Integer numberOfLikes;
    private Integer numberOfComment;
    private Boolean isPublic;
    private Boolean isUpdated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer userId;
    private String userFirstname;
    private String userLastname;
    private String userEmail;
    private String userImagePath;
    private Integer pageId;
    private String pageName;
    private List<String> postImageUrls;
    private boolean userEnabled;
}