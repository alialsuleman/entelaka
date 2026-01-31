package com.ali.antelaka.post.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class CommentAncestorDTO {
    private Integer id;
    private int uiLevel;
    private String text;
    private LocalDateTime createdAt;
    private UserInfoDTO owner;
}