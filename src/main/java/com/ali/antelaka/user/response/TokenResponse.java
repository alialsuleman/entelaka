package com.ali.antelaka.user.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TokenResponse {
    private String token;
    private String refreshToken ;
}
