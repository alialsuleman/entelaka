package com.ali.antelaka.user.service;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.auth.AuthenticationService;
import com.ali.antelaka.token.TokenRepository;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.request.ChangePasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;

    @Autowired
    private AuthenticationService authenticationService ;





    public ApiResponse changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        var apiRes =  ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .build() ;

        if (request.getCurrentPassword() == null )
        {

                if (!LocalDateTime.now().isBefore(user.getMaxTimeToResetPassword())){
                    throw new IllegalStateException("Wrong password");
                }else {
                    apiRes.setMessage("your password has changed !  , please login again");
                    this.authenticationService.revokeAllUserTokens(user);
                }
        }
        else  {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new IllegalStateException("Wrong password");
            }
            if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
                throw new IllegalStateException("Password are not the same");
            }
            apiRes.setMessage("your password has changed ! ");
        }
        user.setMaxTimeToResetPassword(null);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
        return apiRes;
    }
}
