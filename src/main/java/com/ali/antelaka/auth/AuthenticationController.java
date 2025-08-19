package com.ali.antelaka.auth;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.token.Token;
import com.ali.antelaka.token.TokenRepository;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.request.OtpRequest;
import com.ali.antelaka.user.request.RestPasswordOtpRequest;
import com.ali.antelaka.user.request.SenRestPassOtp;
import com.ali.antelaka.user.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static com.ali.antelaka.user.entity.Role.USER;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService service;

  @Autowired
  private ClientRegistrationRepository clientRegistrationRepository;

  @Autowired
  private UserRepository userRepository ;
  @Autowired
  private TokenRepository tokenRepository ;

  @Autowired
  private OtpService otpService ;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String google_client_id;


  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody RegisterRequest request
  ) {
    request.setRole(USER);
    return ResponseEntity.ok(service.register(request));
  }


  // login
  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody AuthenticationRequest request
  ) {
    return ResponseEntity.ok(service.authenticate(request));
  }

  @PostMapping("/refresh-token")
  public void refreshToken(
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {
    service.refreshToken(request, response);
  }



  @PostMapping("/sendresetpasswordotp")
  public ResponseEntity<ApiResponse> sendresetpasswordotp(  @RequestBody SenRestPassOtp request) {

    if (request.getEmail() ==  null )
    {
      return ResponseEntity.ok(
              ApiResponse.<Void>builder()
                      .success(false)
                      .message("Email is required !!!!")
                      .status(HttpStatus.BAD_REQUEST.value())
                      .build());
    }
    var user  =  this.userRepository.findByEmail(request.getEmail()).get() ;
    this.otpService.sendotp(user , true) ;
    ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(true)
            .message("OTP sent successfully")
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.OK.value())
            .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping("/resetpasswordotpchecker")
  public ResponseEntity<ApiResponse> resetpasswordotpchecker(@RequestBody RestPasswordOtpRequest request) {
    return ResponseEntity.ok( this.otpService.checkotpRestpassword(request  )  ) ;
  }



  @GetMapping("/alluser")
  public ResponseEntity<Collection<User>> alluser() {
      return ResponseEntity.ok().body(userRepository.findAll()) ;
  }
  @GetMapping("/alltoken")
  public ResponseEntity<Collection<Token>> alltoken() {
    return ResponseEntity.ok().body(tokenRepository.findAll()) ;
  }



  @GetMapping("/user/{id}")
  public ResponseEntity<Optional<User>> alluser(@PathVariable Integer id) {
    return ResponseEntity.ok().body(userRepository.findById(id)) ;
  }

//  @GetMapping("/oauth2/authorization-url")
//  public String getOAuth2AuthorizationUrl() {
//    ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("google");
//
//    return UriComponentsBuilder
//            .fromUriString(registration.getProviderDetails().getAuthorizationUri())
//            .queryParam("client_id", registration.getClientId())
//            .queryParam("redirect_uri", registration.getRedirectUri())
//            .queryParam("response_type", "code")
//            .queryParam("scope", String.join(" ", registration.getScopes()))
//            .queryParam("state", "your-custom-state")
//            .build()
//            .toUriString();
//  }


}
