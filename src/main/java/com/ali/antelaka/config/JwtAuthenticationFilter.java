package com.ali.antelaka.config;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.token.TokenRepository;
 import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final TokenRepository tokenRepository;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return request.getServletPath().contains("/auth")
            || request.getServletPath().contains("/email/send");
  }


  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {

      System.out.println("JwtAuthenticationFilter");

      if (
              request.getServletPath().contains("/auth") ||
              request.getServletPath().contains("/email/send"))
      {
        filterChain.doFilter(request, response);
        return;
      }

      // Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQG1haWwuY29tIiwiaWF0IjoxNzU1MjY1MzcyLCJleHAiOjE3NTUzNTE3NzJ9.cS3cxxVYXcOK1fkvR0RMIuxVhOw3dFVgTSJvkTD8Ep8
      final String authHeader = request.getHeader("Authorization");
      final String jwt;
      final String userEmail;
      if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
      }
      jwt = authHeader.substring(7);
      userEmail = jwtService.extractUsername(jwt);




      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {


        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
        var isTokenValid = tokenRepository.findByToken(jwt)
            .map(t -> !t.isExpired() && !t.isRevoked())
            .orElse(false);
        boolean isEnabled = userDetails.isEnabled() ;

        if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {



          if (!isEnabled)
          {
            System.out.print("isEnabled");
            Map m = new HashMap<String, String>( ) ;
            m.put("accessToken" , null);
            m.put("refreshToken" , null);
            m.put("Verified" , false);
            sendSuccussResponse(response, "You must confirm your email first.", HttpStatus.OK , m);
            return;
           }


          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );

          authToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request)
          );
          SecurityContextHolder.getContext().setAuthentication(authToken);


        }
      }
      filterChain.doFilter(request, response);
  }


  private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status , Map data  ) throws IOException {


    response.setStatus(status.value());

    ApiResponse<Map<?,?>> apiResponse = new ApiResponse<Map<?,?>>(
            false,
            "Authentication Failed",
            data,
            List.of(message),
            LocalDateTime.now(),
            HttpStatus.FORBIDDEN.value()
    );


 System.out.println("123");
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String jsonResponse = mapper.writeValueAsString(apiResponse);

    response.setContentType("application/json");
    response.getWriter().write(jsonResponse);
  }

  private void sendSuccussResponse(HttpServletResponse response, String message, HttpStatus status , Map data  ) throws IOException {


        response.setStatus(200);

        ApiResponse<Map<?,?>> apiResponse = new ApiResponse<Map<?,?>>(
                true,
                message,
                data,
                List.of(message),
                LocalDateTime.now(),
                HttpStatus.OK.value()
        );


        System.out.println("123");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String jsonResponse = mapper.writeValueAsString(apiResponse);

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
    }

}


