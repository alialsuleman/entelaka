package com.ali.antelaka.user.request;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class UpdateProfileRequest {

    private String firstname;
    private String lastname;
    private String bio;

    private String whatsappLink;
    private String facebookLink;
    private String telegramLink;
    private String linkedinLink;
}
