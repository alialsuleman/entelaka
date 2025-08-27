package com.ali.antelaka.auth;

import com.ali.antelaka.config.JwtService;
import com.ali.antelaka.token.Token;
import com.ali.antelaka.token.TokenRepository;
import com.ali.antelaka.token.TokenType;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  // store user in database and generate token's and return it
  public AuthenticationResponse register(RegisterRequest request) {

    System.out.println(request.getRole());


    var lastUser = repository.findByEmail(request.getEmail()) ;
    if (lastUser.isPresent())
    {
      throw new RuntimeException("This email already exists.")  ;
    }


    var user = User.builder()
        .firstname(request.getFirstname())
        .lastname(request.getLastname())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .enabled(false)
        .build();
    var savedUser = repository.save(user);
//    var jwtToken = jwtService.generateToken(user , false);
//    var refreshToken = jwtService.generateRefreshToken(user);
//    saveUserToken(savedUser, jwtToken);
//    return AuthenticationResponse.builder()
//        .accessToken(jwtToken)
//            .refreshToken(refreshToken)
//        .build();

    var authenticationResponse = AuthenticationResponse.builder()
            .accessToken(null)
            .refreshToken(null)
            .verified(user.isEnabled())
            .build();

    return  authenticationResponse ;
  }


  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    var user = repository.findByEmail(request.getEmail())
        .orElseThrow();
    var jwtToken = jwtService.generateToken(user , false);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
            .refreshToken(refreshToken)
            .verified(user.isEnabled())
        .build();
  }

  // create token
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
       tokenRepository.delete(token);
    });
  }

  public AuthenticationResponse refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {

    try{
      final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
      final String refreshToken;
      final String userEmail;
      if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
        return null;
      }
      refreshToken = authHeader.substring(7);
      userEmail = jwtService.extractUsername(refreshToken);
      if (userEmail != null) {
        var user = this.repository.findByEmail(userEmail)
                .orElseThrow();
        if (jwtService.isTokenValid(refreshToken, user)) {
          var accessToken = jwtService.generateToken(user ,false);
          revokeAllUserTokens(user);
          saveUserToken(user, accessToken);
          var authResponse = AuthenticationResponse.builder()
                  .accessToken(accessToken)
                  .refreshToken(refreshToken)
                  .verified(user.isEnabled())
                  .build();

          return authResponse ;
        }

      }
      return null ;
    } catch (Exception ex)
    {
       System.out.println("asd") ;
        throw  new AuthenticationException(ex.getMessage()) ;
    }

  }




}
