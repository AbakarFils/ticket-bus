package com.ticketbus.payment.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String transactionId;
    private String status;
    private String message;
}
