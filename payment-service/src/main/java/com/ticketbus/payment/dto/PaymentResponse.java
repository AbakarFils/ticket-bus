package com.ticketbus.payment.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String transactionId;
    private String status;
    private String message;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime timestamp;
}
