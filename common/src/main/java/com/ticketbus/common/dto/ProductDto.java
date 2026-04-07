package com.ticketbus.common.dto;

import com.ticketbus.common.domain.ProductType;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private ProductType type;
    private BigDecimal price;
    private int maxUsage;
    private Integer durationDays;
    private String description;
}
