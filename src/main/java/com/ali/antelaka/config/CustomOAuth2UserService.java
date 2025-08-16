package com.ali.antelaka.config;

import com.ali.antelaka.auth.AuthenticationService;
import com.ali.antelaka.auth.RegisterRequest;
import com.ali.antelaka.token.Token;
import com.ali.antelaka.token.TokenRepository;
import com.ali.antelaka.token.TokenType;
import com.ali.antelaka.user.Role;
import com.ali.antelaka.user.User;
import com.ali.antelaka.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private JwtService jwtService ;




    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);


        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String[] names = name != null ? name.split(" ") : new String[]{"", ""};
        String firstname = names.length > 0 ? names[0] : "";
        String lastname = names.length > 1 ? names[1] : "";

        // إنشاء أو تحديث المستخدم في قاعدة البيانات
        var user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .firstname(firstname)
                            .lastname(lastname)
                            .email(email)
                            .password(null)
                            .role(Role.USER)
                            .build();
                    var savedUser = userRepository.save(newUser);

                    return savedUser;
                });

        // توليد JWT token
        String jwtToken = jwtService.generateToken(user);

        // حفظ Token في قاعدة البيانات

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        Map<String, Object> attributes = new HashMap<>();
        attributes.putAll(oAuth2User.getAttributes());
        attributes.put("jwtToken", jwtToken);
        attributes.put("id", user.getId());

        return new DefaultOAuth2User(
                user.getAuthorities(),
                attributes,
                "email" // اسم الـ attribute الذي يستخدم كـ name identifier
        );

     }
    public void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }


    // make all old token Expired and Revoked
    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

}