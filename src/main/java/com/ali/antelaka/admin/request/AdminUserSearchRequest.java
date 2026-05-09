package com.ali.antelaka.admin.request;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSearchRequest {
    private String keyword;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}