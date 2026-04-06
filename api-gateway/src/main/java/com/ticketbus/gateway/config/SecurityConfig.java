package com.ticketbus.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints - no authentication required
                .pathMatchers(
                    "/api/tickets/public-key",
                    "/actuator/**",
                    "/health/**"
                ).permitAll()

                // Admin-only endpoints
                .pathMatchers(
                    "/api/audit/**",
                    "/api/validate/fraud-alerts/**"
                ).hasRole("ADMIN")

                // Operator-only endpoints
                .pathMatchers(
                    "/api/pricing/products",
                    "/api/pricing/operators/**"
                ).hasAnyRole("ADMIN", "OPERATOR")

                // Customer endpoints
                .pathMatchers(
                    "/api/tickets/**",
                    "/api/wallets/**",
                    "/api/payments/**",
                    "/api/customers/me"
                ).hasAnyRole("ADMIN", "OPERATOR", "CUSTOMER")

                // Validation endpoints (for terminals)
                .pathMatchers(
                    "/api/validate/**"
                ).hasAnyRole("ADMIN", "OPERATOR", "VALIDATOR")

                // Any other request needs authentication
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "https://ticketbus.*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
