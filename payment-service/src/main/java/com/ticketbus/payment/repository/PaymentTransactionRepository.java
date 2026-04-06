package com.ticketbus.payment.repository;

import com.ticketbus.common.domain.PaymentTransaction;
import com.ticketbus.common.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<PaymentTransaction> findByTransactionRef(String transactionRef);
    List<PaymentTransaction> findByStatus(PaymentStatus status);
    List<PaymentTransaction> findTop100ByOrderByCreatedAtDesc();
}

