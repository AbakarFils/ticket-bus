package com.ticketbus.wallet.repository;

import com.ticketbus.wallet.domain.WalletBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletBudgetRepository extends JpaRepository<WalletBudget, Long> {
    Optional<WalletBudget> findByUserId(Long userId);
}
