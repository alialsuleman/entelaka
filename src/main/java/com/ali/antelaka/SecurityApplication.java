package com.ali.antelaka;

import com.ali.antelaka.auth.AuthenticationService;
import com.ali.antelaka.auth.RegisterRequest;
import com.ali.antelaka.page.entity.PageType;
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



		};
	}


}
