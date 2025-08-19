package com.ali.antelaka.google;


import com.ali.antelaka.auth.AuthenticationService;
import com.ali.antelaka.config.JwtService;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.service.UserService;
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
    public ResponseEntity<?> googleAuth(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("id_token");

            if (idToken == null || idToken.isEmpty()) {
                return ResponseEntity.badRequest().body("ID token is required");
            }
            GoogleUser googleUser = googleAuthService.verifyIdToken(idToken);

            User user = userService.findOrCreateUser(googleUser);
            String jwtToken = this.jwtService.generateToken(user);


            this.authenticationService.revokeAllUserTokens(user);
            this.authenticationService.saveUserToken(user, jwtToken);



            Map<String, Object> response = new HashMap<>();
            response.put("token", jwtToken);
            response.put("user", user);

            return ResponseEntity.ok(response);

        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }
}

