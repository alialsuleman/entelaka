package com.ali.antelaka.post.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class CommentHistoryDTO {

    private PostSummaryDTO post;

    private MyCommentDTO myComment;

    private List<CommentAncestorDTO> ancestors;

    private boolean hasDeeperParent;
}
