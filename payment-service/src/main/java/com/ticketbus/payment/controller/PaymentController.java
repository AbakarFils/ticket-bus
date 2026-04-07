package com.ticketbus.payment.controller;

<<<<<<< HEAD
import com.ticketbus.payment.domain.PaymentTransaction;
=======
import com.ticketbus.common.dto.PaymentTransactionDto;
>>>>>>> 6a79295c (phase 3)
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
    public ResponseEntity<?> initiatePayment(@RequestBody PaymentRequest request) {
        try {
            return ResponseEntity.ok(paymentService.processPayment(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refund/{transactionId}")
    public ResponseEntity<?> refund(@PathVariable String transactionId) {
        try {
            return ResponseEntity.ok(paymentService.refund(transactionId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<PaymentTransaction>> getAllTransactions() {
        return ResponseEntity.ok(paymentService.getAllTransactions());
    }

    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<List<PaymentTransaction>> getTransactionsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getTransactionsByUser(userId));
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<?> getTransaction(@PathVariable String transactionId) {
        try {
            return ResponseEntity.ok(paymentService.getTransaction(transactionId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/receipt/{transactionId}")
    public ResponseEntity<?> getReceipt(@PathVariable String transactionId) {
        try {
            PaymentTransaction tx = paymentService.getTransaction(transactionId);
            return ResponseEntity.ok(Map.of(
                "receiptNumber", "RCP-" + tx.getId(),
                "transactionId", tx.getTransactionId(),
                "userId", tx.getUserId(),
                "amount", tx.getAmount(),
                "currency", tx.getCurrency(),
                "method", tx.getMethod(),
                "status", tx.getStatus(),
                "reference", tx.getReference() != null ? tx.getReference() : "",
                "issuedAt", tx.getCreatedAt()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
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
