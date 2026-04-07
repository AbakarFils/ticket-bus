package com.ticketbus.wallet.service;

import com.ticketbus.common.domain.Wallet;
import com.ticketbus.common.dto.WalletDto;
import com.ticketbus.wallet.exception.InsufficientFundsException;
import com.ticketbus.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional
    public Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("XAF")
                .monthlySpent(BigDecimal.ZERO)
                .currentMonth(LocalDate.now().getMonthValue())
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
        log.info("Topped up wallet for user {} by {}", userId, amount);
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet debit(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
            .orElseGet(() -> walletRepository.save(Wallet.builder()
                .userId(userId).balance(BigDecimal.ZERO).currency("XAF")
                .monthlySpent(BigDecimal.ZERO).currentMonth(LocalDate.now().getMonthValue())
                .build()));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                "Insufficient funds. Balance: " + wallet.getBalance() + ", Required: " + amount);
        }

        // Reset monthly spending if new month
        resetMonthlySpendingIfNeeded(wallet);

        // Check monthly budget cap
        if (wallet.getMonthlyBudget() != null) {
            BigDecimal projected = (wallet.getMonthlySpent() != null ? wallet.getMonthlySpent() : BigDecimal.ZERO).add(amount);
            if (projected.compareTo(wallet.getMonthlyBudget()) > 0) {
                throw new IllegalStateException(
                    "Monthly budget exceeded. Budget: " + wallet.getMonthlyBudget() +
                    ", Already spent: " + wallet.getMonthlySpent() + ", Requested: " + amount);
            }
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        // Track monthly spending
        if (wallet.getMonthlySpent() == null) {
            wallet.setMonthlySpent(BigDecimal.ZERO);
        }
        wallet.setMonthlySpent(wallet.getMonthlySpent().add(amount));

        // Check and persist budget alert
        if (wallet.getMonthlyBudget() != null && wallet.getAlertThresholdPercent() != null
                && !wallet.isBudgetAlertTriggered()) {
            BigDecimal threshold = wallet.getMonthlyBudget()
                .multiply(BigDecimal.valueOf(wallet.getAlertThresholdPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (wallet.getMonthlySpent().compareTo(threshold) >= 0) {
                wallet.setBudgetAlertTriggered(true);
                log.warn("Budget alert triggered for user {}: spent {} / budget {} (threshold {}%)",
                    userId, wallet.getMonthlySpent(), wallet.getMonthlyBudget(), wallet.getAlertThresholdPercent());
            }
        }

        log.info("Debited {} from wallet for user {}", amount, userId);
        return walletRepository.save(wallet);
    }

    @Transactional
    public WalletDto getBalance(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        resetMonthlySpendingIfNeeded(wallet);
        return toDto(wallet);
    }

    @Transactional
    public Wallet setMonthlyBudget(Long userId, BigDecimal budget, Integer alertThreshold) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setMonthlyBudget(budget);
        if (alertThreshold != null) {
            wallet.setAlertThresholdPercent(alertThreshold);
        }
        log.info("Set monthly budget for user {}: {} XAF, alert at {}%", userId, budget, alertThreshold);
        return walletRepository.save(wallet);
    }

    public List<WalletDto> getAllWallets() {
        return walletRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    private void resetMonthlySpendingIfNeeded(Wallet wallet) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        if (wallet.getCurrentMonth() == null || wallet.getCurrentYear() == null
                || wallet.getCurrentMonth() != currentMonth || wallet.getCurrentYear() != currentYear) {
            wallet.setMonthlySpent(BigDecimal.ZERO);
            wallet.setCurrentMonth(currentMonth);
            wallet.setCurrentYear(currentYear);
            wallet.setBudgetAlertTriggered(false);
        }
    }

    public List<WalletDto> getWalletsWithBudgetAlert() {
        return walletRepository.findAll().stream()
            .map(w -> {
                resetMonthlySpendingIfNeeded(w);
                return toDto(w);
            })
            .filter(WalletDto::isBudgetAlertTriggered)
            .collect(Collectors.toList());
    }

    public Map<String, Object> getBudgetStatus(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        resetMonthlySpendingIfNeeded(wallet);
        WalletDto dto = toDto(wallet);

        Map<String, Object> status = new java.util.HashMap<>();
        status.put("userId", wallet.getUserId());
        status.put("monthlyBudget", wallet.getMonthlyBudget());
        status.put("monthlySpent", wallet.getMonthlySpent());
        status.put("alertTriggered", dto.isBudgetAlertTriggered());
        status.put("alertThresholdPercent", wallet.getAlertThresholdPercent());

        if (wallet.getMonthlyBudget() != null && wallet.getMonthlyBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentUsed = wallet.getMonthlySpent()
                .multiply(BigDecimal.valueOf(100))
                .divide(wallet.getMonthlyBudget(), 2, RoundingMode.HALF_UP);
            status.put("percentUsed", percentUsed);
            status.put("remainingBudget", wallet.getMonthlyBudget().subtract(wallet.getMonthlySpent()));
        } else {
            status.put("percentUsed", BigDecimal.ZERO);
            status.put("remainingBudget", null);
        }
        return status;
    }

    private WalletDto toDto(Wallet wallet) {
        boolean alertTriggered = false;
        if (wallet.getMonthlyBudget() != null && wallet.getAlertThresholdPercent() != null
                && wallet.getMonthlySpent() != null) {
            BigDecimal threshold = wallet.getMonthlyBudget()
                .multiply(BigDecimal.valueOf(wallet.getAlertThresholdPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            alertTriggered = wallet.getMonthlySpent().compareTo(threshold) >= 0;
        }
        return WalletDto.builder()
            .id(wallet.getId())
            .userId(wallet.getUserId())
            .balance(wallet.getBalance())
            .currency(wallet.getCurrency())
            .monthlyBudget(wallet.getMonthlyBudget())
            .monthlySpent(wallet.getMonthlySpent())
            .alertThresholdPercent(wallet.getAlertThresholdPercent())
            .budgetAlertTriggered(alertTriggered)
            .build();
    }
}
