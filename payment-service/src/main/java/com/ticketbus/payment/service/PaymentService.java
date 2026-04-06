package com.ticketbus.payment.service;

import com.ticketbus.payment.domain.PaymentMethod;
import com.ticketbus.payment.domain.PaymentStatus;
import com.ticketbus.payment.domain.PaymentTransaction;
import com.ticketbus.payment.dto.PaymentRequest;
import com.ticketbus.payment.dto.PaymentResponse;
import com.ticketbus.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        String transactionId = UUID.randomUUID().toString();
        PaymentMethod method = request.getMethod() != null ? request.getMethod() : PaymentMethod.MOBILE_MONEY;
        log.info("Processing payment: userId={}, amount={}, method={}, transactionId={}",
            request.getUserId(), request.getAmount(), method, transactionId);

        PaymentTransaction tx = PaymentTransaction.builder()
            .transactionId(transactionId)
            .userId(request.getUserId())
            .amount(request.getAmount())
            .currency(request.getCurrency() != null ? request.getCurrency() : "XAF")
            .method(method)
            .status(PaymentStatus.SUCCESS)
            .reference(request.getReference())
            .description(request.getDescription())
            .build();
        transactionRepository.save(tx);

        return PaymentResponse.builder()
            .transactionId(transactionId)
            .status("SUCCESS")
            .message("Payment processed successfully")
            .amount(request.getAmount())
            .currency(tx.getCurrency())
            .timestamp(LocalDateTime.now())
            .build();
    }

    @Transactional
    public PaymentResponse refund(String transactionId) {
        PaymentTransaction tx = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        if (tx.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Transaction already refunded");
        }
        tx.setStatus(PaymentStatus.REFUNDED);
        transactionRepository.save(tx);
        log.info("Refunded transaction: {}", transactionId);
        return PaymentResponse.builder()
            .transactionId(transactionId)
            .status("REFUNDED")
            .message("Refund processed successfully")
            .amount(tx.getAmount())
            .currency(tx.getCurrency())
            .timestamp(LocalDateTime.now())
            .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentTransaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<PaymentTransaction> getAllTransactions() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public PaymentTransaction getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
    }
}
