package com.ticketbus.payment.service;

import com.ticketbus.payment.dto.PaymentRequest;
import com.ticketbus.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class PaymentService {

    public PaymentResponse processPayment(PaymentRequest request) {
        String transactionId = UUID.randomUUID().toString();
        log.info("Processing payment: userId={}, amount={}, currency={}, reference={}, transactionId={}",
            request.getUserId(), request.getAmount(), request.getCurrency(),
            request.getReference(), transactionId);
        return PaymentResponse.builder()
            .transactionId(transactionId)
            .status("SUCCESS")
            .message("Payment processed successfully (stub mode)")
            .build();
    }
}
