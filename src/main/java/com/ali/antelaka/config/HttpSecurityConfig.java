package com.ali.antelaka.config;

import com.ali.antelaka.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class HttpSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final ObjectMapper objectMapper; // inject it as a Bean, don't create it manually every time

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(req -> req.anyRequest().permitAll())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) ->
                                SecurityContextHolder.clearContext()
                        )
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            try {
                                writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Authentication Failed !!", authException.getMessage());
                            } catch (Exception e) {
                                System.out.println("Failed to write error response, client may have disconnected: {}");

                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            try {
                                writeErrorResponse(response, HttpStatus.FORBIDDEN, "Access Denied", accessDeniedException.getMessage());
                            } catch (Exception e) {
                                System.out.println("Failed to write error response, client may have disconnected: {}");
                            }
                        })
                );

        return http.build();
    }

    // extracted helper method to avoid code duplication
    private void writeErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message,
            String errorDetail
    ) throws Exception {

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                false,
                message,
                null,
                List.of(errorDetail),
                LocalDateTime.now(),
                status.value()
        );

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}