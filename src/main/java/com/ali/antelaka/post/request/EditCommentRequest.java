package com.ali.antelaka.post.request;


import lombok.Data;

@Data
public class EditCommentRequest {
    private Integer commentId;
    private String newText;
}
