package com.ticketbus.common.dto;

import com.ticketbus.common.domain.PaymentStatus;
import com.ticketbus.common.domain.PaymentType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionDto {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentType type;
    private PaymentStatus status;
    private String transactionRef;
    private String externalRef;
    private String paymentMethod;
    private String description;
    private LocalDateTime createdAt;
}

