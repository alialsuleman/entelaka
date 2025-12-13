package com.ali.antelaka.user.dto;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchDTO {

    private Integer id;
    private String firstname;
    private String lastname;
    private String imagePath;
    private String bio;

    private String facebookLink;
    private String linkedinLink;
    private String telegramLink;
    private String whatsappLink;
}