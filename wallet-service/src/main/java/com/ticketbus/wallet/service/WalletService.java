package com.ticketbus.wallet.service;

import com.ticketbus.common.domain.Wallet;
import com.ticketbus.common.dto.WalletDto;
import com.ticketbus.wallet.domain.TransactionType;
import com.ticketbus.wallet.domain.WalletBudget;
import com.ticketbus.wallet.domain.WalletTransaction;
import com.ticketbus.wallet.dto.SetBudgetRequest;
import com.ticketbus.wallet.exception.InsufficientFundsException;
import com.ticketbus.wallet.repository.WalletBudgetRepository;
import com.ticketbus.wallet.repository.WalletRepository;
import com.ticketbus.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletBudgetRepository budgetRepository;

    @Transactional
    public Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("XAF")
                .build();
            return walletRepository.save(wallet);
        });
    }

    @Transactional
    public Wallet topUp(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        transactionRepository.save(WalletTransaction.builder()
            .userId(userId).amount(amount).type(TransactionType.TOPUP)
            .description("Wallet top-up").build());
        log.info("Topped up wallet for user {} by {}", userId, amount);
        return wallet;
    }

    @Transactional
    public Wallet debit(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        checkBudgetCap(userId, amount);
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
            .orElseGet(() -> walletRepository.save(Wallet.builder()
                .userId(userId).balance(BigDecimal.ZERO).currency("XAF").build()));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                "Insufficient funds. Balance: " + wallet.getBalance() + ", Required: " + amount);
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        updateBudgetSpent(userId, amount);
        transactionRepository.save(WalletTransaction.builder()
            .userId(userId).amount(amount).type(TransactionType.DEBIT)
            .description("Ticket purchase").build());
        log.info("Debited {} from wallet for user {}", amount, userId);
        return wallet;
    }

    @Transactional
    public Wallet refund(Long userId, BigDecimal amount, String reference) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        transactionRepository.save(WalletTransaction.builder()
            .userId(userId).amount(amount).type(TransactionType.REFUND)
            .reference(reference).description("Refund: " + reference).build());
        log.info("Refunded {} to wallet for user {}", amount, userId);
        return wallet;
    }

    @Transactional(readOnly = true)
    public WalletDto getBalance(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return WalletDto.builder()
            .id(wallet.getId()).userId(wallet.getUserId())
            .balance(wallet.getBalance()).currency(wallet.getCurrency()).build();
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> getTransactionHistory(Long userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Transactional
    public WalletBudget setBudget(Long userId, SetBudgetRequest req) {
        WalletBudget budget = budgetRepository.findByUserId(userId)
            .orElse(WalletBudget.builder().userId(userId).build());
        if (req.dailyLimit() != null) budget.setDailyLimit(req.dailyLimit());
        if (req.weeklyLimit() != null) budget.setWeeklyLimit(req.weeklyLimit());
        if (req.monthlyLimit() != null) budget.setMonthlyLimit(req.monthlyLimit());
        return budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public WalletBudget getBudget(Long userId) {
        return budgetRepository.findByUserId(userId)
            .orElse(WalletBudget.builder().userId(userId).build());
    }

    private void checkBudgetCap(Long userId, BigDecimal amount) {
        budgetRepository.findByUserId(userId).ifPresent(budget -> {
            resetSpentIfNeeded(budget);
            budgetRepository.save(budget);
            if (budget.getDailyLimit() != null &&
                    budget.getDailySpent().add(amount).compareTo(budget.getDailyLimit()) > 0) {
                throw new IllegalStateException("Daily spending limit exceeded. Limit: "
                    + budget.getDailyLimit() + " XAF");
            }
            if (budget.getWeeklyLimit() != null &&
                    budget.getWeeklySpent().add(amount).compareTo(budget.getWeeklyLimit()) > 0) {
                throw new IllegalStateException("Weekly spending limit exceeded. Limit: "
                    + budget.getWeeklyLimit() + " XAF");
            }
            if (budget.getMonthlyLimit() != null &&
                    budget.getMonthlySpent().add(amount).compareTo(budget.getMonthlyLimit()) > 0) {
                throw new IllegalStateException("Monthly spending limit exceeded. Limit: "
                    + budget.getMonthlyLimit() + " XAF");
            }
        });
    }

    private void updateBudgetSpent(Long userId, BigDecimal amount) {
        budgetRepository.findByUserId(userId).ifPresent(budget -> {
            budget.setDailySpent(budget.getDailySpent().add(amount));
            budget.setWeeklySpent(budget.getWeeklySpent().add(amount));
            budget.setMonthlySpent(budget.getMonthlySpent().add(amount));
            budgetRepository.save(budget);
        });
    }

    private void resetSpentIfNeeded(WalletBudget budget) {
        LocalDateTime now = LocalDateTime.now();
        if (budget.getDailyResetAt() == null || now.isAfter(budget.getDailyResetAt().plusDays(1))) {
            budget.setDailySpent(BigDecimal.ZERO);
            budget.setDailyResetAt(now);
        }
        if (budget.getWeeklyResetAt() == null || now.isAfter(budget.getWeeklyResetAt().plusWeeks(1))) {
            budget.setWeeklySpent(BigDecimal.ZERO);
            budget.setWeeklyResetAt(now);
        }
        if (budget.getMonthlyResetAt() == null || now.isAfter(budget.getMonthlyResetAt().plusMonths(1))) {
            budget.setMonthlySpent(BigDecimal.ZERO);
            budget.setMonthlyResetAt(now);
        }
    }
}

