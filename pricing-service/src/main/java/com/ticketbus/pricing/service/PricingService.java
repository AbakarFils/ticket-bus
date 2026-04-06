package com.ticketbus.pricing.service;

import com.ticketbus.common.domain.Product;
import com.ticketbus.common.domain.ProductType;
import com.ticketbus.common.dto.ProductDto;
import com.ticketbus.pricing.dto.BestFareRecommendation;
import com.ticketbus.pricing.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final ProductRepository productRepository;

    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public BestFareRecommendation recommend(Long userId, int expectedTripsPerMonth) {
        List<Product> products = productRepository.findByActiveTrue();

        Product bestProduct = null;
        BigDecimal bestCost = null;
        String bestReason = null;

        for (Product product : products) {
            BigDecimal cost = calculateMonthlyCost(product, expectedTripsPerMonth);
            if (cost == null) continue;
            if (bestCost == null || cost.compareTo(bestCost) < 0) {
                bestCost = cost;
                bestProduct = product;
                bestReason = buildReason(product, expectedTripsPerMonth, cost);
            }
        }

        if (bestProduct == null) {
            throw new IllegalStateException("No active products available for recommendation");
        }

        ProductDto dto = toProductDto(bestProduct);
        return BestFareRecommendation.builder()
            .recommendedProduct(dto)
            .estimatedCost(bestCost)
            .reason(bestReason)
            .build();
    }

    private BigDecimal calculateMonthlyCost(Product product, int trips) {
        if (product.getType() == ProductType.UNIT) {
            return product.getPrice().multiply(BigDecimal.valueOf(trips));
        } else if (product.getType() == ProductType.PACK) {
            if (product.getMaxUsage() <= 0) return null;
            int packsNeeded = (int) Math.ceil((double) trips / product.getMaxUsage());
            return product.getPrice().multiply(BigDecimal.valueOf(packsNeeded));
        } else if (product.getType() == ProductType.PASS) {
            if (product.getDurationDays() != null && product.getDurationDays() >= 30) {
                return product.getPrice();
            } else if (product.getDurationDays() != null && product.getDurationDays() == 7) {
                return product.getPrice().multiply(BigDecimal.valueOf(4));
            } else if (product.getDurationDays() != null && product.getDurationDays() == 1) {
                return product.getPrice().multiply(BigDecimal.valueOf(22));
            }
        }
        return null;
    }

    private String buildReason(Product product, int trips, BigDecimal cost) {
        return String.format("For %d trips/month, '%s' costs an estimated %s XAF/month", trips, product.getName(), cost.setScale(0, RoundingMode.HALF_UP));
    }

    private ProductDto toProductDto(Product product) {
        return ProductDto.builder()
            .id(product.getId())
            .name(product.getName())
            .type(product.getType())
            .price(product.getPrice())
            .maxUsage(product.getMaxUsage())
            .durationDays(product.getDurationDays())
            .build();
    }
}
