package com.ali.antelaka.user.service;


import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.auth.AuthenticationService;
import com.ali.antelaka.config.JwtService;
import com.ali.antelaka.mail.EmailService;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.request.OtpRequest;
import com.ali.antelaka.user.request.RestPasswordOtpRequest;
import com.ali.antelaka.user.response.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private UserRepository userRepository ;

    @Autowired
    private EmailService emailService ;

    @Autowired
    private AuthenticationService authenticationService ;

    @Autowired
    private JwtService  jwtService ;

    @Value("${application.security.jwt.resetpasswordexpiration}")
    private long jwtResetPasswordExpiration;

    // done
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }


    public void sendotp(Principal connectedUser, boolean resetpassword ) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        this.sendotp(user , resetpassword);
    }

    public void sendotp(User user, boolean resetpassword ) {

        String otp = generateOtp() ;
        if (resetpassword) {
            user.setResetPasswordOtp(otp );
            user.setResetPasswordOtpExpirationTime(LocalDateTime.now().plusMinutes(30));
        }
        else {
            user.setOtp(otp);
            user.setOtpExpirationTime(LocalDateTime.now().plusMinutes(30));
        }
        userRepository.save(user);
        this.emailService.sendEmail(user.getEmail() , "OTP Verification", "Your OTP code is:"+otp);
    }

    public ApiResponse<?> checkotp( OtpRequest request, Principal connectedUser) {


        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (user.getOtp() == null || user.getOtpExpirationTime() == null) {
            return
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("No OTP found. Request a new one.")
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build();

        }

        boolean isValid = user.getOtp().equals(request.getOtp())
                && LocalDateTime.now().isBefore(user.getOtpExpirationTime());

        if (isValid) {
            user.setOtp(null);
            user.setOtpExpirationTime(null);
            user.setEnabled(true );
            userRepository.save(user);

            return
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("OTP is valid")
                            .status(HttpStatus.OK.value())
                            .build() ;

        } else {
            return
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Invalid or expired OTP")
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .build();

        }
    }


    public ApiResponse<?> checkotpRestpassword(RestPasswordOtpRequest request) {

        if (request.getEmail()== null)
        {
            return
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Email is required !!!!")
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build();
        }
        var user1 = this.userRepository.findByEmail(request.getEmail());
        if (user1.isEmpty())
        {
            return
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Email is not registered !!!!")
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build();
        }
        var user = user1.get() ;


        if (user.getResetPasswordOtp() == null || user.getResetPasswordOtpExpirationTime() == null) {
            return
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("No OTP found. Request a new one.")
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build();

        }

        boolean isValid = user.getResetPasswordOtp().equals(request.getOtp())
                && LocalDateTime.now().isBefore(user.getResetPasswordOtpExpirationTime());

        if (isValid) {
            user.setResetPasswordOtp(null);
            user.setResetPasswordOtpExpirationTime(null);
            user.setMaxTimeToResetPassword(LocalDateTime.now().plusMinutes(jwtResetPasswordExpiration/60000));

            String token = this.jwtService.generateRestPasswordToken(user) ;
            this.authenticationService.revokeAllUserTokens(user);
            this.authenticationService.saveUserToken(user, token);
            userRepository.save(user);



            return
                    ApiResponse.builder()
                            .success(true)
                            .data(new TokenResponse(token , null))
                            .message("OTP is valid , you have to reset password in 15. min")
                            .status(HttpStatus.OK.value())
                            .build() ;

        } else {
            return
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Invalid or expired OTP")
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .build();

        }
    }





}
