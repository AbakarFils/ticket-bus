package com.ticketbus.customer.config;

<<<<<<< HEAD
=======
import org.springframework.beans.factory.annotation.Value;
>>>>>>> 6a79295c (phase 3)
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
<<<<<<< HEAD
import org.springframework.security.config.http.SessionCreationPolicy;
=======
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
>>>>>>> 6a79295c (phase 3)
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

<<<<<<< HEAD
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.requestMatchers("/**").permitAll());
        return http.build();
    }
}
=======
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

>>>>>>> 6a79295c (phase 3)
