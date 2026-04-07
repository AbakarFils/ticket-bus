package com.ticketbus.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.ticketbus.common.domain")
@EnableJpaRepositories(basePackages = "com.ticketbus.audit.repository")
@EnableAsync
@EnableScheduling
public class AuditServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditServiceApplication.class, args);
    }
}

