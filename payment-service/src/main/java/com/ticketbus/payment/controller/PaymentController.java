package com.ticketbus.payment.controller;

import com.ticketbus.common.dto.PaymentTransactionDto;
import com.ticketbus.payment.dto.PaymentRequest;
import com.ticketbus.payment.dto.PaymentResponse;
import com.ticketbus.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentTransactionDto>> getTransactionsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getTransactionsByUser(userId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PaymentTransactionDto>> getRecentTransactions() {
        return ResponseEntity.ok(paymentService.getRecentTransactions());
    }

    @PostMapping("/refund/{transactionRef}")
    public ResponseEntity<?> refund(@PathVariable String transactionRef) {
        try {
            return ResponseEntity.ok(paymentService.refund(transactionRef));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> callback(@RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(Map.of("status", "received"));
    }
}
