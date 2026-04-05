package com.ticketbus.pricing.controller;

import com.ticketbus.common.domain.Product;
import com.ticketbus.pricing.dto.BestFareRecommendation;
import com.ticketbus.pricing.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getActiveProducts() {
        return ResponseEntity.ok(pricingService.getActiveProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(pricingService.getProduct(id));
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(pricingService.createProduct(product));
    }

    @GetMapping("/recommend")
    public ResponseEntity<BestFareRecommendation> recommend(
            @RequestParam Long userId,
            @RequestParam int tripsPerMonth) {
        return ResponseEntity.ok(pricingService.recommend(userId, tripsPerMonth));
    }
}
