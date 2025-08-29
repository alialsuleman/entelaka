package com.ali.antelaka.google;


import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.auth.AuthenticationResponse;
import com.ali.antelaka.auth.AuthenticationService;
import com.ali.antelaka.config.JwtService;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class GoogleController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(  @RequestBody Map<String, String> request) {

            String idToken = request.get("id_token");

            if (idToken == null || idToken.isEmpty()) {


                return ResponseEntity.badRequest().body( ApiResponse.<Void>builder()
                        .success(false)
                        .message("login successfully")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .build());
            }
            GoogleUser googleUser = googleAuthService.verifyIdToken(idToken);

            User user = userService.findOrCreateUser(googleUser);
            String jwtToken = this.jwtService.generateToken(user , false);
            String refreshToken = this.jwtService.generateRefreshToken(user) ;

            this.authenticationService.revokeAllUserTokens(user);
            this.authenticationService.saveUserToken(user, jwtToken);


        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder().
        accessToken(jwtToken). refreshToken(refreshToken).verified(true) .build();


            return ResponseEntity.ok(

                    ApiResponse.builder()
                            .success(true)
                            .data(authenticationResponse)
                            .message("login successfully")
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build()


            );


    }
}

