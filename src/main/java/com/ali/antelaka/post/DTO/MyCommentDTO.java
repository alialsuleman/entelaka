package com.ali.antelaka.post.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class MyCommentDTO {
    private Integer id;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likes;
    private int replies;
    private int uiLevel;
    private Integer replyToCommentId;
}
