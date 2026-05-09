package com.ali.antelaka.auth.Service;


import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.auth.OtpProperties;
import com.ali.antelaka.auth.dto.AuthenticationResponse;
import com.ali.antelaka.auth.dto.OtpContext;
import com.ali.antelaka.auth.dto.OtpType;
import com.ali.antelaka.config.JwtService;
import com.ali.antelaka.exceptionHandler.exception.BadRequestException;
import com.ali.antelaka.exceptionHandler.exception.NotFoundException;
import com.ali.antelaka.mail.EmailService;

import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.page.entity.PageType;
import com.ali.antelaka.page.PageRepository;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.request.CheckOtpRequest;
import com.ali.antelaka.user.request.SendOtpRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class OtpService {

    @Autowired
    private UserRepository userRepository ;
    @Autowired
    private EmailService emailService ;
    @Autowired
    private AuthenticationService authenticationService ;
    @Autowired
    private PageRepository pageRepository ;
    @Autowired
    private JwtService  jwtService ;
    @Value("${application.security.jwt.resetpasswordexpiration}")
    private long jwtResetPasswordExpiration;


    @Autowired
    private OtpProperties otpProperties;




    // done
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }




    public void sendOtp(SendOtpRequest request) {
        User user  = getUser( request.getEmail());
        boolean resetPassword = request.getSetpassword()==1 ;

        OtpContext context = resolveContext(user, resetPassword);
        validateNotBanned(context);

        String otp = generateOtp();
        incrementAndApplyBanIfNeeded(user, context);
        applyOtp(user, context, otp);

        userRepository.save(user);
        emailService.sendEmail(user.getEmail(), "OTP Verification", "Your OTP code is: " + otp);

    }


    private User getUser (String email){
        var user  =  this.userRepository.findByEmail(email) ;
        if (!user.isPresent()) throw new NotFoundException("you have to register first" , null) ;
        return user.get() ;
    }



    private OtpContext resolveContext(User user, boolean resetPassword) {
        if (resetPassword) {
            return OtpContext.builder()
                    .banTime(user.getResetPasswordOTPSendingBanTime())
                    .lastSentAt(user.getLastResetPasswordOTPSentAt())
                    .numberOfSending(user.getNumberOfresetPasswordOtpSending())
                    .type(OtpType.RESET_PASSWORD)
                    .build();
        } else {
            return OtpContext.builder()
                    .banTime(user.getOTPSendingBanTime())
                    .lastSentAt(user.getLastOtpSentAt())
                    .numberOfSending(user.getNumberOfOtpSending())
                    .type(OtpType.EMAIL_CONFIRMATION)
                    .build();
        }
    }

    private void validateNotBanned(OtpContext context) {
        if (context.getBanTime() != null &&
                LocalDateTime.now().isBefore(context.getBanTime())) {
            throw new BadRequestException("Too many attempts. Try again later.");
        }
    }


    private void incrementAndApplyBanIfNeeded(User user, OtpContext context) {
        int newCount = context.getNumberOfSending() + 1;
        int banMinutes = otpProperties.getShortBanMinutes();

        if (newCount % otpProperties.getMaxAttempts() == 0) {
            boolean recentActivity = context.getLastSentAt() != null &&
                    Duration.between(context.getLastSentAt(), LocalDateTime.now())
                            .toMinutes() < otpProperties.getResetWindowMinutes();

            if (recentActivity) {
                banMinutes = otpProperties.getLongBanMinutes();
            } else {
                newCount = 0; // reset العداد
            }
        }

        applyBanAndCount(user, context.getType(), newCount, banMinutes);
    }


    private void applyBanAndCount(User user, OtpType type, int count, int banMinutes) {
        LocalDateTime banTime = LocalDateTime.now().plusMinutes(banMinutes);
        LocalDateTime now = LocalDateTime.now();

        if (type == OtpType.RESET_PASSWORD) {
            user.setNumberOfresetPasswordOtpSending(count);
            user.setResetPasswordOTPSendingBanTime(banTime);
            user.setLastResetPasswordOTPSentAt(now);
        } else {
            user.setNumberOfOtpSending(count);
            user.setOTPSendingBanTime(banTime);
            user.setLastOtpSentAt(now);
        }
    }


    private void applyOtp(User user, OtpContext context, String otp) {
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(otpProperties.getExpiryMinutes());

        if (context.getType() == OtpType.RESET_PASSWORD) {
            user.setResetPasswordOtp(otp);
            user.setResetPasswordOtpExpirationTime(expiry);
        } else {
            user.setOtp(otp);
            user.setOtpExpirationTime(expiry);
        }
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
            if (user.getResetPasswordOtp() == null ) {
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


            if (user.getOtp() == null ) {
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
            user.setEnabled(true );


            user.setOtp(null);
            user.setResetPasswordOtp(null);


            user.setResetPasswordOtpExpirationTime(LocalDateTime.now().minusMinutes(10) );
            user.setOtpExpirationTime(LocalDateTime.now().minusMinutes(10) );


            user.setAttempts(0);
            user.setResetPasswordOtpAttempts(0) ;

            user.setNumberOfOtpSending(0);
            user.setNumberOfresetPasswordOtpSending(0);

            user.setResetPasswordOTPSendingBanTime(LocalDateTime.now().minusMinutes(10) );
            user.setOTPSendingBanTime(LocalDateTime.now().minusMinutes(10) );

            user.setLastOtpSentAt(LocalDateTime.now().minusMinutes(10) );
            user.setLastResetPasswordOTPSentAt(LocalDateTime.now().minusMinutes(10) );

            if (isForRestPassword) {
                user.setMaxTimeToResetPassword(
                        LocalDateTime.now().plusMinutes(jwtResetPasswordExpiration/60000));
            }




            String token = this.jwtService.generateToken(user , isForRestPassword) ;

            this.authenticationService.revokeAllUserTokens(user);
            this.authenticationService.saveUserToken(user, token);
            var savedUser = userRepository.save(user);
            if (!isForRestPassword) {
                PageEntity publicUserPage = PageEntity.builder()
                        .id(savedUser.getId())
                        .user(savedUser)
                        .pageType(PageType.PUBLIC.name())
                        .description("Hi there")
                        .build();
                this.pageRepository.save(publicUserPage);
            }





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
            int numberOfAttempts =0 ;

            if (isForRestPassword)
            {
                user.setResetPasswordOtpAttempts(user.getResetPasswordOtpAttempts()+1);
                numberOfAttempts =  user.getResetPasswordOtpAttempts() ;
            }

            else{
                user.setAttempts(user.getAttempts()+1);
                numberOfAttempts =  user.getAttempts() ;
            }

            this.userRepository.save(user) ;

            Map<String,Integer> m = new HashMap<>( ) ;
            m.put("numberOfAttempts" , numberOfAttempts) ;
            m.put("numberOfAttemptsRemaining" ,  (3 - numberOfAttempts) ) ;

            if (numberOfAttempts >=3 )
            {
                user.setResetPasswordOtp(null);
                user.setOtp(null);

                user.setResetPasswordOtpExpirationTime(LocalDateTime.now());
                user.setOtpExpirationTime(LocalDateTime.now());

                user.setAttempts(0);
                user.setResetPasswordOtpAttempts(0);

                this.userRepository.save(user) ;

                return
                        ApiResponse.<Map>builder()
                                .success(false)
                                .message("You have exceeded the allowed number of attempts. Please resend the code again.")
                                .data(m)
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .build();
            }
            else {

                return
                        ApiResponse.<Map>builder()
                                .success(false)
                                .message("Invalid or expired OTP")
                                .data(m)
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .build();
            }


        }
    }





}
