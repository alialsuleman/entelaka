package com.ali.antelaka.post.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentHistoryDTO {
    private Integer id;
    private String text;
    private LocalDateTime createdAt;
    private Integer parentCommentId;
    private PostSummaryDTO postSummaryDTO;
}
