package com.ali.antelaka.user.request;


import lombok.Data;

@Data
public class RestPasswordOtpRequest {
    private String otp;
    private String email ;
}
