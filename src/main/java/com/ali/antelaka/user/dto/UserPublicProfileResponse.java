package com.ali.antelaka.user.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPublicProfileResponse {
    private Integer id;
    private String firstname;
    private String lastname;
    private String bio;
    private String imagePath;
    private String role;

    private String email;

    private int postsCount;
    private int followersCount;
    private int followingCount;


    private boolean isMyProfile;

    private boolean isFollowing;
}