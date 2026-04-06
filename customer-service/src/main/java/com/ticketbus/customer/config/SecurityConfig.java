package com.ticketbus.customer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().permitAll() // Phase 3 MVP: allow all, tighten with Keycloak later
            );

        // Enable JWT validation when Keycloak is configured
        if (issuerUri != null && !issuerUri.isBlank()) {
            try {
                http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
            } catch (Exception ignored) {
                // Keycloak not available, continue without JWT
            }
        }

        return http.build();
    }
}

