package com.ali.antelaka.post.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDTO {
    private Integer userId;
    private String username;
    private String userImagePath;
    private boolean me; // هل هو المستخدم الحالي؟
    private boolean iFollowingHim; // هل يتابعه المستخدم الحالي؟
}