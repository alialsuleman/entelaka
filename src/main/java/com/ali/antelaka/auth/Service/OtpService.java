package com.ali.antelaka.auth.Service;


import com.ali.antelaka.auth.OtpProperties;
import com.ali.antelaka.auth.dto.AuthenticationResponse;
import com.ali.antelaka.auth.OtpContext;
import com.ali.antelaka.auth.OtpType;
import com.ali.antelaka.config.JwtService;
import com.ali.antelaka.exceptionHandler.exception.BadRequestException;
import com.ali.antelaka.exceptionHandler.exception.NotFoundException;
import com.ali.antelaka.mail.EmailService;

import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.page.entity.PageType;
import com.ali.antelaka.page.PageRepository;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.dto.CheckOtpRequest;
import com.ali.antelaka.user.dto.SendOtpRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
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





    public AuthenticationResponse checkOtp(CheckOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Email is not registered"));

        boolean isForResetPassword = request.getSetpassword() == 1;

        validateOtp(user, request.getOtp(), isForResetPassword);
        AuthenticationResponse response = buildAuthResponse(user, isForResetPassword);
        resetUserOtpState(user, isForResetPassword);

        return response;
    }

    private void validateOtp(User user, String otpInput, boolean isForResetPassword) {
        String storedOtp = isForResetPassword ? user.getResetPasswordOtp() : user.getOtp();
        LocalDateTime expirationTime = isForResetPassword
                ? user.getResetPasswordOtpExpirationTime()
                : user.getOtpExpirationTime();

        if (storedOtp == null) {
            throw new NotFoundException("No OTP found. Request a new one.");
        }

        boolean isValid = storedOtp.equals(otpInput)
                && LocalDateTime.now().isBefore(expirationTime);

        if (!isValid) {
            handleFailedAttempt(user, isForResetPassword);
        }
    }
    private void handleFailedAttempt(User user, boolean isForResetPassword) {
        int attempts;

        if (isForResetPassword) {
            user.setResetPasswordOtpAttempts(user.getResetPasswordOtpAttempts() + 1);
            attempts = user.getResetPasswordOtpAttempts();
        } else {
            user.setAttempts(user.getAttempts() + 1);
            attempts = user.getAttempts();
        }

        userRepository.save(user);

        if (attempts >= otpProperties.getMaxAttempts()) {
            clearOtpState(user, isForResetPassword);
            throw new BadRequestException("Exceeded allowed attempts. Please request a new OTP.");
        }

        throw new BadRequestException(
                String.format("Invalid OTP. %d attempts remaining.", otpProperties.getMaxAttempts() - attempts)
        );
    }

    private AuthenticationResponse buildAuthResponse(User user, boolean isForResetPassword) {
        String token = jwtService.generateToken(user, isForResetPassword);
        authenticationService.revokeAllUserTokens(user);
        authenticationService.saveUserToken(user, token);

        if (isForResetPassword) {
            return new AuthenticationResponse(token, null, user.isEnabled());
        }

        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthenticationResponse(token, refreshToken, user.isEnabled());
    }
    private void resetUserOtpState(User user, boolean isForResetPassword) {
        LocalDateTime past = LocalDateTime.now().minusMinutes(10);

        user.setOtp(null);
        user.setResetPasswordOtp(null);
        user.setOtpExpirationTime(past);
        user.setResetPasswordOtpExpirationTime(past);
        user.setAttempts(0);
        user.setResetPasswordOtpAttempts(0);
        user.setNumberOfOtpSending(0);
        user.setNumberOfresetPasswordOtpSending(0);
        user.setOTPSendingBanTime(past);
        user.setResetPasswordOTPSendingBanTime(past);
        user.setLastOtpSentAt(past);
        user.setLastResetPasswordOTPSentAt(past);

        if (isForResetPassword) {
            user.setMaxTimeToResetPassword(
                    LocalDateTime.now().plusMinutes(jwtResetPasswordExpiration / 60000));
        } else {
            user.setEnabled(true);
            createPublicPage(user);
        }

        userRepository.save(user);
    }

    private void createPublicPage(User user) {
        PageEntity publicUserPage = PageEntity.builder()
                .id(user.getId())
                .user(user)
                .pageType(PageType.PUBLIC.name())
                .description("Hi there")
                .build();
        pageRepository.save(publicUserPage);
    }

    private void clearOtpState(User user, boolean isForResetPassword) {
        if (isForResetPassword) {
            user.setResetPasswordOtp(null);
            user.setResetPasswordOtpExpirationTime(LocalDateTime.now());
            user.setResetPasswordOtpAttempts(0);
        } else {
            user.setOtp(null);
            user.setOtpExpirationTime(LocalDateTime.now());
            user.setAttempts(0);
        }
        userRepository.save(user);
    }








}
