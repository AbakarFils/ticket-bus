package com.ticketbus.wallet.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(precision = 19, scale = 2)
    private BigDecimal dailyLimit;

    @Column(precision = 19, scale = 2)
    private BigDecimal weeklyLimit;

    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal dailySpent = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal weeklySpent = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal monthlySpent = BigDecimal.ZERO;

    private LocalDateTime dailyResetAt;
    private LocalDateTime weeklyResetAt;
    private LocalDateTime monthlyResetAt;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
