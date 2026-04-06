package com.ticketbus.common.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "fare_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Long productId;

    private String zone;

    private String timeSlot;

    private String passengerCategory;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(precision = 19, scale = 2)
    private BigDecimal dailyCap;

    @Column(precision = 19, scale = 2)
    private BigDecimal weeklyCap;

    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyCap;

    @Builder.Default
    private boolean active = true;
}

