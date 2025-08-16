package com.ali.antelaka.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import static com.ali.antelaka.user.Permission.ADMIN_CREATE;
import static com.ali.antelaka.user.Permission.ADMIN_DELETE;
import static com.ali.antelaka.user.Permission.ADMIN_READ;
import static com.ali.antelaka.user.Permission.ADMIN_UPDATE;
import static com.ali.antelaka.user.Permission.MANAGER_CREATE;
import static com.ali.antelaka.user.Permission.MANAGER_DELETE;
import static com.ali.antelaka.user.Permission.MANAGER_READ;
import static com.ali.antelaka.user.Permission.MANAGER_UPDATE;
import static com.ali.antelaka.user.Role.ADMIN;
import static com.ali.antelaka.user.Role.MANAGER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
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
                        req.requestMatchers("/api/v1/auth/**").permitAll() // سماح للجميع بالوصول إلى endpoints المصادقة
                                .requestMatchers("/oauth2/authorization/google").permitAll()
                                .anyRequest().authenticated() // أي طلب آخر يحتاج إلى مصادقة
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout ->
                        logout.logoutUrl("/api/v1/auth/logout")
                                .addLogoutHandler(logoutHandler)
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                ) ;
        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler((request, response, authentication) -> {
                            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
                            String jwtToken = (String) oauthUser.getAttributes().get("jwtToken");

                            response.setContentType("application/json");
                            response.getWriter().write("{\"token\":\"" + jwtToken + "\"}");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"OAuth2 Authentication Failed\"}");
                        })
                ) ;

        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                })
        );
        return http.build();
    }


}
