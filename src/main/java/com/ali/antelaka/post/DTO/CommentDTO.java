package com.ali.antelaka.post.DTO;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDTO {
    private Integer id;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName; // معلومات إضافية من المستخدم
    private Integer userId;
    private String userAvatar ;
    private Integer numberOfReplies ;
}