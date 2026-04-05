package com.ticketbus.wallet.controller;

import com.ticketbus.common.domain.Wallet;
import com.ticketbus.common.dto.WalletDto;
import com.ticketbus.wallet.exception.InsufficientFundsException;
import com.ticketbus.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
            return ResponseEntity.ok(WalletDto.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .build());
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
            return ResponseEntity.ok(WalletDto.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .build());
        } catch (InsufficientFundsException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
