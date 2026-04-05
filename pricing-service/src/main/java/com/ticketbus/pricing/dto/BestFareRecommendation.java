package com.ticketbus.pricing.dto;

import com.ticketbus.common.dto.ProductDto;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BestFareRecommendation {
    private ProductDto recommendedProduct;
    private BigDecimal estimatedCost;
    private String reason;
}
