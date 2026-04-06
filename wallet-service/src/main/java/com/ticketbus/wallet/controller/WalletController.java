package com.ticketbus.wallet.controller;

import com.ticketbus.common.dto.WalletDto;
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

    @GetMapping
    public ResponseEntity<List<WalletDto>> getAllWallets() {
        return ResponseEntity.ok(walletService.getAllWallets());
    }

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
            walletService.topUp(userId, amount);
            return ResponseEntity.ok(walletService.getBalance(userId));
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
            walletService.debit(userId, amount);
            return ResponseEntity.ok(walletService.getBalance(userId));
        } catch (InsufficientFundsException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{userId}/budget")
    public ResponseEntity<?> setMonthlyBudget(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        try {
            BigDecimal budget = body.get("monthlyBudget") != null
                ? new BigDecimal(body.get("monthlyBudget").toString()) : null;
            Integer alertThreshold = body.get("alertThresholdPercent") != null
                ? Integer.parseInt(body.get("alertThresholdPercent").toString()) : null;
            walletService.setMonthlyBudget(userId, budget, alertThreshold);
            return ResponseEntity.ok(walletService.getBalance(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
