package com.ali.antelaka.post.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLevelDTO {
    private Integer id;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer repliedUserId;
    private String repliedUsername;
    private Integer numberOfLikes;
    private Integer numberOfSubComment;
    private UserInfoDTO commentOwner;
}
