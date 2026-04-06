package com.ticketbus.payment.repository;

import com.ticketbus.payment.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByTransactionId(String transactionId);
    List<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
