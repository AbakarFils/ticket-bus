package com.ticketbus.pricing;

import com.ticketbus.common.domain.Product;
import com.ticketbus.common.domain.ProductType;
import com.ticketbus.pricing.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.existsByActiveTrue()) {
            log.info("Products already initialized, skipping");
            return;
        }
        List<Product> defaults = List.of(
            Product.builder().name("Ticket unitaire").type(ProductType.UNIT).price(new BigDecimal("500")).maxUsage(1).durationDays(null).description("Ticket valable pour un seul trajet").active(true).build(),
            Product.builder().name("Pack 10 trajets").type(ProductType.PACK).price(new BigDecimal("4500")).maxUsage(10).durationDays(null).description("Pack de 10 trajets à prix réduit").active(true).build(),
            Product.builder().name("Carnet 10 voyages").type(ProductType.CARNET).price(new BigDecimal("4000")).maxUsage(10).durationDays(90).description("Carnet de 10 voyages valable 90 jours").active(true).build(),
            Product.builder().name("Carnet 20 voyages").type(ProductType.CARNET).price(new BigDecimal("7500")).maxUsage(20).durationDays(90).description("Carnet de 20 voyages valable 90 jours").active(true).build(),
            Product.builder().name("Pass journée").type(ProductType.PASS).price(new BigDecimal("1500")).maxUsage(999).durationDays(1).description("Voyages illimités pendant 24h").active(true).build(),
            Product.builder().name("Pass semaine").type(ProductType.PASS).price(new BigDecimal("8000")).maxUsage(999).durationDays(7).description("Voyages illimités pendant 7 jours").active(true).build(),
            Product.builder().name("Pass mois").type(ProductType.PASS).price(new BigDecimal("25000")).maxUsage(999).durationDays(30).description("Voyages illimités pendant 30 jours").active(true).build()
        );
        productRepository.saveAll(defaults);
        log.info("Initialized {} default products", defaults.size());
    }
}
