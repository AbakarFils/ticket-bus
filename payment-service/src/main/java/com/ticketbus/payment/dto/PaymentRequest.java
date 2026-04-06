package com.ticketbus.payment.dto;

import com.ticketbus.payment.domain.PaymentMethod;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String reference;
    private String description;
    private PaymentMethod method;
}
