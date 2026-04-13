package com.ali.antelaka.admin;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminPostSearchRequest {
    private String keyword;
    private String tag;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}