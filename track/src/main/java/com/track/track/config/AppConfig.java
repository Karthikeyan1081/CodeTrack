package com.track.track.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // ---------------- REST TEMPLATE ----------------

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // ---------------- PASSWORD ENCODER ----------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}