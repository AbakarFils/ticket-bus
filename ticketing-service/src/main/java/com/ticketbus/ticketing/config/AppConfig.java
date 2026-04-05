package com.ticketbus.ticketing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Value("${services.wallet-url:http://localhost:8082}")
    private String walletUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
