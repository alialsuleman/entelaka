package com.ali.antelaka.user.dto ;

import com.ali.antelaka.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ManagerResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerResponse {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String imagePath;
    private String role;
    private boolean enabled;
}