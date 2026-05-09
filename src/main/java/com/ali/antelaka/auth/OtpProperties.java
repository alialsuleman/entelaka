package com.ali.antelaka.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {
    private int maxAttempts;
    private int shortBanMinutes;
    private int resetWindowMinutes;
    private int longBanMinutes;
    private int expiryMinutes;
}