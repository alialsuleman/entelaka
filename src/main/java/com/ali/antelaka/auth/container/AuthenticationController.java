package com.ali.antelaka.auth.container;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.auth.dto.AuthenticationRequest;
import com.ali.antelaka.auth.dto.AuthenticationResponse;
import com.ali.antelaka.auth.Service.AuthenticationService;
import com.ali.antelaka.auth.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.ali.antelaka.user.entity.Role.USER;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService service;



  @PostMapping("/register")
  public ResponseEntity<ApiResponse<?>> register(
          @Valid @RequestBody RegisterRequest request
  ) {
    request.setRole(USER);
    ApiResponse<?> response = ApiResponse.builder()
            .success(true)
            .message("register successfully! now you have to confirm your email")
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






}
