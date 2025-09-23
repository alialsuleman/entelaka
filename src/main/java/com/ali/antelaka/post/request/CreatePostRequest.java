package com.ali.antelaka.post.request;

import com.ali.antelaka.post.entity.PostImage;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreatePostRequest {

    private String text;
    private String tag;
    private List<Integer> postImageIds;

}
