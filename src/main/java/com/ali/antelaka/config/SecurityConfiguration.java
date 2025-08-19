package com.ali.antelaka.config;

import com.ali.antelaka.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;


import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {


    @Autowired
    private JwtService jwtService ;
    @Autowired
    private  CustomOAuth2UserService customOAuth2UserService ;


    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/oauth2/authorization/google").permitAll()
                                .requestMatchers("/email/**").permitAll()
                                .requestMatchers("/swagger-ui/index.html").permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))

                .logout(logout ->
                        logout.logoutUrl("/auth/logout")
                                .addLogoutHandler(logoutHandler)
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                ).authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); ;
        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler((request, response, authentication) -> {
                            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
                            String jwtToken = (String) oauthUser.getAttributes().get("jwtToken");



                            ApiResponse<String> apiResponse = new ApiResponse<>(
                                    true,
                                    "Authentication successful",
                                    jwtToken,
                                    null,
                                    LocalDateTime.now(),
                                    HttpStatus.OK.value()
                            );


                            ObjectMapper mapper = new ObjectMapper();
                            mapper.registerModule(new JavaTimeModule());
                            String jsonResponse = mapper.writeValueAsString(apiResponse);

                            response.setContentType("application/json");
                            response.getWriter().write(jsonResponse);


                        })
                        .failureHandler((request, response, exception) -> {

                            ApiResponse<Void> apiResponse = new ApiResponse<>(
                                    false,
                                    "OAuth2 Authentication Failed",
                                    null,
                                    List.of(exception.getMessage()),
                                    LocalDateTime.now(),
                                    HttpServletResponse.SC_UNAUTHORIZED
                            );


                            ObjectMapper mapper = new ObjectMapper();
                            mapper.registerModule(new JavaTimeModule());
                            String jsonResponse = mapper.writeValueAsString(apiResponse);


                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(jsonResponse);


                        })
                ) ;

//        http.exceptionHandling(exception ->
//                exception.authenticationEntryPoint((request, response, authException) -> {
//
//                    ApiResponse<Void> apiResponse = new ApiResponse<>(
//                            false,
//                            "OAuth2 Authentication Failed",
//                            null,
//                            List.of(authException.getMessage()),
//                            LocalDateTime.now(),
//                            HttpServletResponse.SC_UNAUTHORIZED
//                    );
//
//
//                    ObjectMapper mapper = new ObjectMapper();
//                    mapper.registerModule(new JavaTimeModule());
//                    String jsonResponse = mapper.writeValueAsString(apiResponse);
//
//
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.setContentType("application/json");
//                    response.getWriter().write(jsonResponse);
//                })  );
        http.exceptionHandling(exception -> {
            exception
                     .authenticationEntryPoint((request, response, authException) -> {
                        throw new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authentication Failed: " + authException.getMessage()
                        );
                    })
                     .accessDeniedHandler((request, response, accessDeniedException) -> {
                        throw new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Access Denied: " + accessDeniedException.getMessage()
                        );
                    });
        });

        return http.build();
    }


}
