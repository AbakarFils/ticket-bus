package com.ticketbus.ticketing.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletClient {

    private final RestTemplate restTemplate;

    @Value("${services.wallet-url:http://localhost:8082}")
    private String walletUrl;

    public BigDecimal getBalance(Long userId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> wallet = restTemplate.getForObject(
                walletUrl + "/api/wallets/" + userId, Map.class);
            if (wallet != null && wallet.get("balance") != null) {
                return new BigDecimal(wallet.get("balance").toString());
            }
        } catch (Exception e) {
            log.error("Failed to get wallet balance for user {}: {}", userId, e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public void debit(Long userId, BigDecimal amount) {
        Map<String, Object> body = Map.of("amount", amount);
        try {
            org.springframework.http.ResponseEntity<Map> response = restTemplate.postForEntity(
                walletUrl + "/api/wallets/" + userId + "/debit", body, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> errorBody = (Map<String, Object>) response.getBody();
                String msg = errorBody != null ? String.valueOf(errorBody.getOrDefault("error", "Debit failed")) : "Debit failed";
                throw new IllegalStateException(msg);
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            throw new IllegalStateException("Wallet debit failed: " + responseBody);
        }
    }
}
