package com.ali.antelaka.post.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSummaryDTO {
    private Integer id;
    private String textSnippet;
    private String publisherName;
    private String publisherAvatar;
}