package com.ali.antelaka.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
public class GoogleAuthService {

    private static final String GOOGLE_TOKEN_VERIFICATION_URL = "https://oauth2.googleapis.com/tokeninfo";

    @Autowired
    private RestTemplate restTemplate  ;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    public GoogleUser verifyIdToken(String idToken) {

        try {


            ResponseEntity<Map> response = restTemplate.getForEntity(
                    GOOGLE_TOKEN_VERIFICATION_URL + "?id_token={idToken}",
                    Map.class,
                    idToken
            );


            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Google token verification failed");
            }

            Map<String, Object> tokenInfo = response.getBody();
            System.out.println(tokenInfo);
            //  un comment this when you set the real google client id
//            String audience = (String) tokenInfo.get("aud");
//            if (!audience.equals(googleClientId)) {
//                throw new RuntimeException("Invalid token audience");
//            }
            Object emailVerifiedObj = tokenInfo.get("email_verified");
            boolean emailVerified;

            if (emailVerifiedObj instanceof Boolean) {
                emailVerified = (Boolean) emailVerifiedObj;
            } else if (emailVerifiedObj instanceof String) {
                emailVerified = Boolean.parseBoolean((String) emailVerifiedObj);
            } else {
                emailVerified = false;
            }
             if ( !emailVerified) {
                throw new RuntimeException("Email not verified by Google");
            }

            String email = (String) tokenInfo.get("email");
            if (email == null || email.isEmpty()) {
                throw new RuntimeException("Email not found in token");
            }


            GoogleUser googleUser = new GoogleUser();
            googleUser.setEmail((String) tokenInfo.get("email"));
            googleUser.setName((String) tokenInfo.get("name"));
            googleUser.setPicture((String) tokenInfo.get("picture"));
            googleUser.setSub((String) tokenInfo.get("sub")); // Google's unique user ID

            return googleUser;
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Invalid Google token");
        } catch (Exception e) {
            throw new RuntimeException("Token verification error: " + e.getMessage());
        }

    }
}