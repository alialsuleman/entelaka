package com.ali.antelaka.config;

import com.ali.antelaka.auditing.ApplicationAuditAware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {



  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }




  @Bean
  public AuditorAware<Integer> auditorAware() {
    return new ApplicationAuditAware();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }



}
