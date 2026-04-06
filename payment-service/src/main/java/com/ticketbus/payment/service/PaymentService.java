package com.ticketbus.payment.service;

import com.ticketbus.common.domain.*;
import com.ticketbus.common.dto.PaymentTransactionDto;
import com.ticketbus.payment.dto.PaymentRequest;
import com.ticketbus.payment.dto.PaymentResponse;
import com.ticketbus.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    @Value("${services.wallet-url:http://localhost:8082}")
    private String walletUrl;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        String transactionRef = UUID.randomUUID().toString();

        PaymentType paymentType = PaymentType.TOPUP;
        if (request.getType() != null) {
            try {
                paymentType = PaymentType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        PaymentTransaction tx = PaymentTransaction.builder()
            .userId(request.getUserId())
            .amount(request.getAmount())
            .currency(request.getCurrency() != null ? request.getCurrency() : "XAF")
            .type(paymentType)
            .status(PaymentStatus.PENDING)
            .transactionRef(transactionRef)
            .externalRef(request.getReference())
            .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH")
            .description("Payment " + paymentType + " for user " + request.getUserId())
            .build();
        tx = transactionRepository.save(tx);

        log.info("Processing payment: userId={}, amount={}, type={}, ref={}",
            request.getUserId(), request.getAmount(), paymentType, transactionRef);

        try {
            // Simulate external payment gateway (always succeeds in stub mode)
            // In production, call external Mobile Money / Card API here

            tx.setStatus(PaymentStatus.SUCCESS);
            transactionRepository.save(tx);

            // If TOPUP, credit the wallet
            if (paymentType == PaymentType.TOPUP) {
                try {
                    restTemplate.postForEntity(
                        walletUrl + "/api/wallets/" + request.getUserId() + "/topup",
                        Map.of("amount", request.getAmount()),
                        Map.class);
                    log.info("Wallet topped up for user {} with amount {}", request.getUserId(), request.getAmount());
                } catch (Exception e) {
                    log.error("Failed to top up wallet for user {}: {}", request.getUserId(), e.getMessage());
                    // Transaction succeeded but wallet credit failed - needs reconciliation
                }
            }

            return PaymentResponse.builder()
                .transactionId(transactionRef)
                .status("SUCCESS")
                .message("Payment processed successfully")
                .build();

        } catch (Exception e) {
            tx.setStatus(PaymentStatus.FAILED);
            transactionRepository.save(tx);
            log.error("Payment processing failed", e);
            return PaymentResponse.builder()
                .transactionId(transactionRef)
                .status("FAILED")
                .message("Payment processing failed: " + e.getMessage())
                .build();
        }
    }

    public List<PaymentTransactionDto> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<PaymentTransactionDto> getRecentTransactions() {
        return transactionRepository.findTop100ByOrderByCreatedAtDesc().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse refund(String transactionRef) {
        PaymentTransaction original = transactionRepository.findByTransactionRef(transactionRef)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionRef));

        if (original.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Can only refund successful transactions");
        }

        String refundRef = UUID.randomUUID().toString();
        PaymentTransaction refundTx = PaymentTransaction.builder()
            .userId(original.getUserId())
            .amount(original.getAmount())
            .currency(original.getCurrency())
            .type(PaymentType.REFUND)
            .status(PaymentStatus.SUCCESS)
            .transactionRef(refundRef)
            .externalRef(transactionRef)
            .paymentMethod(original.getPaymentMethod())
            .description("Refund for transaction " + transactionRef)
            .build();
        transactionRepository.save(refundTx);

        original.setStatus(PaymentStatus.REFUNDED);
        transactionRepository.save(original);

        // Credit wallet back
        try {
            restTemplate.postForEntity(
                walletUrl + "/api/wallets/" + original.getUserId() + "/topup",
                Map.of("amount", original.getAmount()),
                Map.class);
        } catch (Exception e) {
            log.error("Failed to credit refund to wallet: {}", e.getMessage());
        }

        return PaymentResponse.builder()
            .transactionId(refundRef)
            .status("SUCCESS")
            .message("Refund processed for " + transactionRef)
            .build();
    }

    private PaymentTransactionDto toDto(PaymentTransaction tx) {
        return PaymentTransactionDto.builder()
            .id(tx.getId())
            .userId(tx.getUserId())
            .amount(tx.getAmount())
            .currency(tx.getCurrency())
            .type(tx.getType())
            .status(tx.getStatus())
            .transactionRef(tx.getTransactionRef())
            .externalRef(tx.getExternalRef())
            .paymentMethod(tx.getPaymentMethod())
            .description(tx.getDescription())
            .createdAt(tx.getCreatedAt())
            .build();
    }
}
