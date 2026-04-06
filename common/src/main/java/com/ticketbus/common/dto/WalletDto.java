package com.ticketbus.common.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletDto {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private BigDecimal monthlyBudget;
    private BigDecimal monthlySpent;
    private Integer alertThresholdPercent;
    private boolean budgetAlertTriggered;
}
