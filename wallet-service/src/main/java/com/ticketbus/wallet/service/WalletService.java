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
                .build();
            return walletRepository.save(wallet);
        });
    }

    @Transactional
    public Wallet topUp(Long userId, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        log.info("Topped up wallet for user {} by {}", userId, amount);
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet debit(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
            .orElseGet(() -> getOrCreateWallet(userId));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                "Insufficient funds. Balance: " + wallet.getBalance() + ", Required: " + amount);
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        log.info("Debited {} from wallet for user {}", amount, userId);
        return walletRepository.save(wallet);
    }

    public WalletDto getBalance(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return WalletDto.builder()
            .id(wallet.getId())
            .userId(wallet.getUserId())
            .balance(wallet.getBalance())
            .currency(wallet.getCurrency())
            .build();
    }
}
