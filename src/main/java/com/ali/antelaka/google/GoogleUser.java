package com.ali.antelaka.google;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUser {
    private String sub;
    private String email;
    private String name;
    private String picture;
    private boolean emailVerified;
}