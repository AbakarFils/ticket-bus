package com.ticketbus.wallet.controller;

import com.ticketbus.common.domain.Wallet;
import com.ticketbus.common.dto.WalletDto;
import com.ticketbus.wallet.domain.WalletBudget;
import com.ticketbus.wallet.domain.WalletTransaction;
import com.ticketbus.wallet.dto.SetBudgetRequest;
import com.ticketbus.wallet.exception.InsufficientFundsException;
import com.ticketbus.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{userId}")
    public ResponseEntity<WalletDto> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getBalance(userId));
    }

    @PostMapping("/{userId}/topup")
    public ResponseEntity<?> topUp(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        Object amountVal = body.get("amount");
        if (amountVal == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required field: amount"));
        }
        try {
            BigDecimal amount = new BigDecimal(amountVal.toString());
            Wallet wallet = walletService.topUp(userId, amount);
            return ResponseEntity.ok(toDto(wallet));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<?> debit(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        Object amountVal = body.get("amount");
        if (amountVal == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required field: amount"));
        }
        try {
            BigDecimal amount = new BigDecimal(amountVal.toString());
            Wallet wallet = walletService.debit(userId, amount);
            return ResponseEntity.ok(toDto(wallet));
        } catch (InsufficientFundsException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{userId}/refund")
    public ResponseEntity<?> refund(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        Object amountVal = body.get("amount");
        String reference = body.get("reference") != null ? body.get("reference").toString() : "REFUND";
        if (amountVal == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required field: amount"));
        }
        try {
            BigDecimal amount = new BigDecimal(amountVal.toString());
            Wallet wallet = walletService.refund(userId, amount, reference);
            return ResponseEntity.ok(toDto(wallet));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<WalletTransaction>> getTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getTransactionHistory(userId));
    }

    @GetMapping("/{userId}/budget")
    public ResponseEntity<WalletBudget> getBudget(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getBudget(userId));
    }

    @PostMapping("/{userId}/budget")
    public ResponseEntity<WalletBudget> setBudget(@PathVariable Long userId,
                                                   @RequestBody SetBudgetRequest request) {
        return ResponseEntity.ok(walletService.setBudget(userId, request));
    }

    private WalletDto toDto(Wallet wallet) {
        return WalletDto.builder()
            .id(wallet.getId()).userId(wallet.getUserId())
            .balance(wallet.getBalance()).currency(wallet.getCurrency()).build();
    }
}

