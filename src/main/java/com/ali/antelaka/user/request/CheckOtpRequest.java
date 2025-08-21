package com.ali.antelaka.user.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckOtpRequest {

    @NotBlank(message = "otp required")
    private String otp;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email required")
    private String email ;
    private Integer  setpassword ;
}
