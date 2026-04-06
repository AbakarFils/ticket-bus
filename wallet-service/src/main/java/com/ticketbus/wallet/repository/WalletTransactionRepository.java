package com.ticketbus.wallet.repository;

import com.ticketbus.wallet.domain.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByUserIdOrderByTimestampDesc(Long userId);
}
