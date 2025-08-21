package com.ali.antelaka;

import com.ali.antelaka.auth.AuthenticationService;
import com.ali.antelaka.auth.RegisterRequest;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.crypto.SecretKey;

import java.util.Base64;

import static com.ali.antelaka.user.entity.Role.*;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class SecurityApplication {

	public static void main(String[] args) {

		SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
		System.out.println("JWT Secret Key: " + encodedKey);

//		Role adminRole = Role.ADMIN;
//		List<SimpleGrantedAuthority> adminAuthorities = adminRole.getAuthorities();
//		System.out.println(adminAuthorities);
//		for (SimpleGrantedAuthority auth : adminAuthorities) {
//			System.out.println(auth.equals(new SimpleGrantedAuthority("admin:create")));
//		}
		SpringApplication.run(SecurityApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			AuthenticationService service
	) {
		return args -> {
			var admin = RegisterRequest.builder()
					.firstname("Admin")
					.lastname("Admin")
					.email("admin@mail.com")
					.password("password")
					.role(ADMIN)
					.build();
			//System.out.println("Admin token: " + service.register(admin).getAccessToken());
			System.out.println("Admin roles: " + admin.getRole());

			var manager = RegisterRequest.builder()
					.firstname("Admin")
					.lastname("Admin")
					.email("manager@mail.com")
					.password("password")
					.role(MANAGER)
					.build();
		//	System.out.println("Manager token: " + service.register(manager).getAccessToken());
			System.out.println("Manager roles: " + manager.getRole());

			var user = RegisterRequest.builder()
					.firstname("user")
					.lastname("user")
					.email("user@mail.com")
					.password("password")
					.role(USER)
					.build();
		//	System.out.println("USer token: " + service.register(user).getAccessToken());
			System.out.println("USer roles: " + user.getRole());

		};
	}
}
