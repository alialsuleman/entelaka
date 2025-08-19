package com.ali.antelaka.user;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.request.ChangePasswordRequest;
import com.ali.antelaka.user.request.OtpRequest;
import com.ali.antelaka.user.service.OtpService;
import com.ali.antelaka.user.service.UserService;
import lombok.RequiredArgsConstructor;
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



    @GetMapping("/sendotp")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> sendotp(Principal connectedUser) {

        this.otpService.sendotp(connectedUser , false) ;
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("OTP sent successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    // Check OTP
    @PostMapping("/checkotp")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> checkotp(@RequestBody OtpRequest request, Principal connectedUser) {

        return ResponseEntity.ok(this.otpService.checkotp(request, connectedUser)) ;
    }

//    //todo
//    @PostMapping("/forgetpassword")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
//    public ResponseEntity<ApiResponse> forgetpassword( Principal connectedUser) {
//
//        return null;
//    }
//    //todo
//    @PostMapping("/forgetpassword")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
//    public ResponseEntity<ApiResponse> forgetpassword2( Principal connectedUser) {
//
//        return null;
//    }



    @GetMapping
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
          @RequestBody ChangePasswordRequest request,
          Principal connectedUser
    ) {
        return ResponseEntity.ok().body(service.changePassword( request, connectedUser)  );
    }
}
