package com.ticketbus.ticketing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ticketbus.common.domain.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Generates dynamic QR codes that rotate every N seconds (TOTP-like).
 * The rotatingNonce changes each window, making screenshot-based fraud useless.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicQrService {

    private final QrSigningService qrSigningService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ticketing.qr.rotation-seconds:30}")
    private int rotationSeconds;

    /**
     * Generate the current rotating nonce for a ticket based on its secret.
     */
    public String computeRotatingNonce(String ticketSecret, long windowIndex) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(ticketSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(Long.toString(windowIndex).getBytes(StandardCharsets.UTF_8));
            // Take first 8 bytes as hex string
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute rotating nonce", e);
        }
    }

    /**
     * Get the current time window index.
     */
    public long getCurrentWindowIndex() {
        return System.currentTimeMillis() / (rotationSeconds * 1000L);
    }

    /**
     * Build a live (rotating) QR payload for the current time window.
     * This is called by the mobile app every ~30s to refresh the displayed QR.
     */
    public String buildLiveQrPayload(Ticket ticket) {
        try {
            long windowIndex = getCurrentWindowIndex();
            String rotatingNonce = computeRotatingNonce(ticket.getSecret(), windowIndex);

            // Build the payload that will be signed: includes rotatingNonce
            String sigPayload = ticket.getId() + "|" +
                ticket.getUserId() + "|" +
                ticket.getNonce() + "|" +
                ticket.getValidFrom() + "|" +
                ticket.getValidUntil() + "|" +
                ticket.getMaxUsage() + "|" +
                rotatingNonce;

            String signature = qrSigningService.sign(sigPayload);

            ObjectNode node = objectMapper.createObjectNode();
            node.put("ticketId", ticket.getId());
            node.put("userId", String.valueOf(ticket.getUserId()));
            node.put("nonce", ticket.getNonce());
            node.put("validFrom", ticket.getValidFrom().toString());
            node.put("validUntil", ticket.getValidUntil().toString());
            node.put("maxUsage", ticket.getMaxUsage());
            node.put("zone", ticket.getZone() != null ? ticket.getZone() : "");
            node.put("rotatingNonce", rotatingNonce);
            node.put("windowIndex", windowIndex);
            node.put("signature", signature);

            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build live QR payload", e);
        }
    }

    public int getRotationSeconds() {
        return rotationSeconds;
    }
}

