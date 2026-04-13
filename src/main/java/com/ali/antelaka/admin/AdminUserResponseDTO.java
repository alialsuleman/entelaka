package com.ali.antelaka.admin;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponseDTO {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String imagePath;
    private String role;
    private String bio;
    private boolean enabled;
    private LocalDateTime createdAt;
    private int postsCount;
    private int followersCount;
    private int followingCount;
}