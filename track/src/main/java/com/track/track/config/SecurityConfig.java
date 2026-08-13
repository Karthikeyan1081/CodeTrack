package com.track.track.config;

import com.track.track.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // LOGIN ONLY PUBLIC
                        .requestMatchers(
                                "/auth/login"
                        ).permitAll()

                        // ADMIN APIs
                        .requestMatchers(
                                "/auth/admin/**"
                        ).hasAuthority("ADMIN")

                        // ADVISOR & ADMIN APIs
                        .requestMatchers(
                                "/auth/advisor/**"
                        ).hasAnyAuthority(
                                "ADVISOR",
                                "ADMIN"
                        )
                        .requestMatchers("/advisors/**")
                        .hasAnyAuthority("ADMIN")

                        // STATIC PAGES
                        // STATIC PAGES
                        .requestMatchers(
                                "/*.html",
                                "/*.css",
                                "/*.js",
                                "/static/**",
                                "/assets/**",
                                "/images/**",
                                "/favicon.ico",
                                "/",
                                "/index.html",
                                "/student-dashboard.html",
                                "/advisor-dashboard.html",
                                "/admin-dashboard.html",
                                "/analytics.html"
                        ).permitAll()

                        // EVERYTHING ELSE REQUIRES LOGIN
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://127.0.0.1:5500",
                "http://localhost:5500",
                "http://localhost:8080",
                "https://codetrack-new.vercel.app",
                "https://codetrack-new-*.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }
}