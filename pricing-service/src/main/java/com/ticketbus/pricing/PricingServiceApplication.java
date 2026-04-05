package com.ticketbus.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.ticketbus.common.domain")
@EnableJpaRepositories(basePackages = "com.ticketbus.pricing.repository")
public class PricingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PricingServiceApplication.class, args);
    }
}
