package com.ticketbus.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD

@SpringBootApplication
=======
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.ticketbus.common.domain")
@EnableJpaRepositories(basePackages = "com.ticketbus.customer.repository")
>>>>>>> 6a79295c (phase 3)
public class CustomerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
<<<<<<< HEAD
=======

>>>>>>> 6a79295c (phase 3)
