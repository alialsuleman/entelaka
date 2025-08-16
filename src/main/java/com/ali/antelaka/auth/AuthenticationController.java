package com.ali.antelaka.auth;

import com.ali.antelaka.user.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

import static com.ali.antelaka.user.Role.USER;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService service;

  @Autowired
  private ClientRegistrationRepository clientRegistrationRepository;

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




  @GetMapping("/oauth2/authorization-url")
  public String getOAuth2AuthorizationUrl() {
    ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("google");

    return UriComponentsBuilder
            .fromUriString(registration.getProviderDetails().getAuthorizationUri())
            .queryParam("client_id", registration.getClientId())
            .queryParam("redirect_uri", registration.getRedirectUri())
            .queryParam("response_type", "code")
            .queryParam("scope", String.join(" ", registration.getScopes()))
            .queryParam("state", "your-custom-state")
            .build()
            .toUriString();
  }


}
