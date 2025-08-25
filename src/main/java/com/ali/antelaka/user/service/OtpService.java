package com.ali.antelaka.user.service;


import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.auth.AuthenticationResponse;
import com.ali.antelaka.auth.AuthenticationService;
import com.ali.antelaka.config.JwtService;
import com.ali.antelaka.mail.EmailService;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.request.CheckOtpRequest;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
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

        LocalDateTime now = LocalDateTime.now();

        if (user.getLastOtpSentAt() != null &&
                Duration.between(user.getLastOtpSentAt(), now).toSeconds() < 60) {
            throw new RuntimeException("You can request OTP only once per minute.");
        }
        user.setLastOtpSentAt(now);
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


    public ApiResponse<?> checkOtp(CheckOtpRequest request) {

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



        boolean isForRestPassword =  false ;
        if (request.getSetpassword() ==1 ) isForRestPassword = true ;

        boolean isValid = false ;

        if (isForRestPassword )
        {
            if (user.getResetPasswordOtp() == null || user.getResetPasswordOtpExpirationTime() == null) {
                return
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message("No OTP found. Request a new one.")
                                .status(HttpStatus.BAD_REQUEST.value())
                                .build();

            }

            isValid = user.getResetPasswordOtp().equals(request.getOtp())
                    && LocalDateTime.now().isBefore(user.getResetPasswordOtpExpirationTime());
        }
       else  {


            if (user.getOtp() == null || user.getOtpExpirationTime() == null) {
                return
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message("No OTP found. Request a new one.")
                                .status(HttpStatus.BAD_REQUEST.value())
                                .build();

            }
            System.out.println(LocalDateTime.now()  + " " +user.getOtpExpirationTime());
            isValid = user.getOtp().equals(request.getOtp())
                    && LocalDateTime.now().isBefore( user.getOtpExpirationTime());
        }

        if (isValid) {
            user.setResetPasswordOtp(null);
            user.setResetPasswordOtpExpirationTime(null);
            user.setOtpExpirationTime(null);
            user.setOtp(null);
            user.setEnabled(true );
            user.setAttempts(0);

            if (isForRestPassword)
                user.setMaxTimeToResetPassword(
                        LocalDateTime.now().plusMinutes(jwtResetPasswordExpiration/60000));


            String token = this.jwtService.generateToken(user , isForRestPassword) ;

            this.authenticationService.revokeAllUserTokens(user);
            this.authenticationService.saveUserToken(user, token);
            userRepository.save(user);

            AuthenticationResponse res  ;
            if (isForRestPassword) res  = new AuthenticationResponse(token , null , user.isEnabled() );
            else {
                String refreshToken  = this.jwtService.generateRefreshToken(user) ;
                res  = new AuthenticationResponse(token , refreshToken , user.isEnabled() );
            }


            return
                    ApiResponse.builder()
                            .success(true)
                            .data(res)
                            .message("OTP is valid")
                            .status(HttpStatus.OK.value())
                            .build() ;

        } else {
            if (user.getAttempts() == null )
            {
                user.setAttempts(0) ;
            };
            user.setAttempts(user.getAttempts() +1) ;
            this.userRepository.save(user) ;
            if (user.getAttempts() >=3 )
            {
                user.setResetPasswordOtp(null);
                user.setResetPasswordOtpExpirationTime(null);
                user.setOtpExpirationTime(null);
                user.setOtp(null);
                user.setAttempts(0);

                this.userRepository.save(user) ;
                return
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message("You have exceeded the allowed number of attempts. Please resend the code again.")
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .build();
            }
            else {

                return
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message("Invalid or expired OTP")
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .build();
            }


        }
    }





}
