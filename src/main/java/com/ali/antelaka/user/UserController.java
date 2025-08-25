package com.ali.antelaka.user;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.request.ChangePasswordRequest;
import com.ali.antelaka.user.service.OtpService;
import com.ali.antelaka.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;








@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
//@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService service;

    @Autowired
    private UserRepository userRepository ;


    @Autowired
    private OtpService otpService;


    @GetMapping("/hello")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String hello (Authentication authentication)
    {
        String username = authentication.getName();

        var authorities = authentication.getAuthorities();

        var user = (User) authentication.getPrincipal();
        System.out.println(user);
        return "Hello "+ user.getUsername();
    }


    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
          Principal connectedUser
    ) {
        return ResponseEntity.ok().body(service.changePassword( request, connectedUser)  );
    }
}
