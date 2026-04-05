package com.ticketbus.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.ticketbus.common.domain")
@EnableJpaRepositories(basePackages = "com.ticketbus.ticketing.repository")
public class TicketingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketingServiceApplication.class, args);
    }
}
