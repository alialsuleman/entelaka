package com.ali.antelaka.auth;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.token.Token;
import com.ali.antelaka.token.TokenRepository;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.request.CheckOtpRequest;
  import com.ali.antelaka.user.request.SendOtpRequest;
import com.ali.antelaka.user.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
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
  public ResponseEntity<ApiResponse<Map<?,?>>> register(
          @Valid @RequestBody RegisterRequest request
  ) {
    request.setRole(USER);


    ApiResponse<Map<?,?>> response = ApiResponse.<Map<?,?>>builder()
            .success(true)
            .message("register successfully")
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.OK.value())
            .data (service.register(request))
            .build();

    return ResponseEntity.ok(response);

  }


  // login
  @PostMapping("/authenticate")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
          @Valid  @RequestBody AuthenticationRequest request
  ) {
    ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
            .success(true)
            .message("login successfully")
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.OK.value())
            .data (service.authenticate(request))
            .build();

    return ResponseEntity.ok(response);


   }

  @GetMapping("/refresh-token")
  public ResponseEntity<?> refreshToken(
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {

    var res=  service.refreshToken(request, response);
    if (res == null)
    {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(
              ApiResponse.<Void>builder()
                      .success(false)
                      .message("login again")
                      .data(null)
                      .status(HttpStatus.UNAUTHORIZED.value())
                      .build());
    }
    else {


      ApiResponse<AuthenticationResponse> response2 = ApiResponse.<AuthenticationResponse>builder()
              .success(true)
              .message("done !")
              .timestamp(LocalDateTime.now())
              .status(HttpStatus.OK.value())
              .data(res)
              .build();

      return ResponseEntity.ok(response2);

    }

  }



  @PostMapping("/sendotp")
  public ResponseEntity<ApiResponse> sendOtp( @Valid @RequestBody SendOtpRequest request) {

    boolean flag = false ;
    if (request.getSetpassword() == 1 )
       flag = true ;


    if (request.getEmail() ==  null  )
    {
      return ResponseEntity.badRequest().body(
              ApiResponse.<Void>builder()
                      .success(false)
                      .message("Email is required !!!!")
                      .status(HttpStatus.BAD_REQUEST.value())
                      .build());
    }

    var user1  =  this.userRepository.findByEmail(request.getEmail()) ;

    if (!user1.isPresent())
    {
      ApiResponse<Void> response = ApiResponse.<Void>builder()
              .success(false)
              .message("you have to register first")
              .timestamp(LocalDateTime.now())
              .status(HttpStatus.BAD_REQUEST.value())
              .build();

      return ResponseEntity.status(response.getStatus()).body(response);
    }
    var  user = user1.get() ;
    this.otpService.sendotp(user , flag) ;

    ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(true)
            .message("OTP sent successfully")
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.OK.value())
            .build();

    return ResponseEntity.ok(response);
  }


  @PostMapping("/checkotp")
  public ResponseEntity<ApiResponse> checkotp(@Valid @RequestBody CheckOtpRequest request) {
    ApiResponse res = this.otpService.checkOtp( request ) ;
    return ResponseEntity.status(res.getStatus()).body(res) ;
  }





  // testing section
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

  @DeleteMapping("/deleteall")
  public ResponseEntity<String> deleteall() {
    this.tokenRepository.deleteAll();
    this.userRepository.deleteAll();

    return ResponseEntity.ok().body("done !") ;
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
