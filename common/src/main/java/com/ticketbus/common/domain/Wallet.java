package com.ticketbus.common.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    private String currency = "XAF";

    /** Monthly budget limit (null = no limit) */
    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyBudget;

    /** Amount spent in current month */
    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal monthlySpent = BigDecimal.ZERO;

    /** Month tracker (resets spending) */
    private Integer currentMonth;

    /** Year tracker (resets spending on year change) */
    private Integer currentYear;

    /** Whether budget alert has been triggered this month */
    @Builder.Default
    private boolean budgetAlertTriggered = false;

    /** Budget alert threshold percentage (e.g., 80 = alert at 80%) */
    private Integer alertThresholdPercent;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
