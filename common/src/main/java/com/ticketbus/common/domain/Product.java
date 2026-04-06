package com.ticketbus.common.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    private int maxUsage;

    private Integer durationDays;

    private Long operatorId;

    private String zoneCode;

    @Builder.Default
    private boolean active = true;
}
