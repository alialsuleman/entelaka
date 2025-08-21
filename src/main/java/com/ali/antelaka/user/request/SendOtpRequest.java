package com.ali.antelaka.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SendOtpRequest {


    @Email(message = "Email must be valid")
    @NotBlank(message = "Email required")
    private String email ;
    private Integer setpassword;


}
