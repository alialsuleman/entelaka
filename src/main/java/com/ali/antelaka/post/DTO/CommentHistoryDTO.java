package com.ali.antelaka.post.DTO;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CommentHistoryDTO {

    private PostSummaryDTO post;

    private MyCommentDTO myComment;

    private List<CommentAncestorDTO> ancestors;

    private boolean hasDeeperParent;
}
