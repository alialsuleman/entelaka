package com.ali.antelaka.auth.container;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.auth.dto.AuthenticationResponse;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.dto.CheckOtpRequest;
import com.ali.antelaka.user.dto.SendOtpRequest;
import com.ali.antelaka.auth.Service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class OtpController {

    final private UserRepository userRepository ;
    final private OtpService otpService ;


    @PostMapping("/sendotp")
    public ResponseEntity<ApiResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        this.otpService.sendOtp(request) ;
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("OTP sent successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build());
    }


    @PostMapping("/checkotp")
    public ResponseEntity<ApiResponse> checkotp(@Valid @RequestBody CheckOtpRequest request) {
        AuthenticationResponse data = otpService.checkOtp(request);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("OTP is valid")
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build());
    }

}
