package com.ticketbus.validation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbus.common.domain.*;
import com.ticketbus.validation.dto.TicketScanRequest;
import com.ticketbus.validation.dto.ValidationResponse;
import com.ticketbus.validation.repository.FraudAlertRepository;
import com.ticketbus.validation.repository.TicketRepository;
import com.ticketbus.validation.repository.ValidationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final QrVerificationService qrVerificationService;
    private final AntiReplayService antiReplayService;
    private final TicketRepository ticketRepository;
    private final ValidationEventRepository validationEventRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final FraudAlertSseService fraudAlertSseService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Value("${validation.collision-window-minutes:5}")
    private int collisionWindowMinutes;

    @Transactional
    public ValidationResponse validate(TicketScanRequest req) {
        Long ticketId = null;
        try {
            JsonNode json = objectMapper.readTree(req.getQrPayload());
            ticketId = json.get("ticketId").asLong();
            String nonce = json.get("nonce").asText();
            String validUntilStr = json.get("validUntil").asText();
            int maxUsage = json.get("maxUsage").asInt();
            String productType = json.has("productType") ? json.get("productType").asText() : "";

            String userId = json.get("userId").asText();
            String validFrom = json.get("validFrom").asText();
            String sigPayload = ticketId + "|" + userId + "|" + nonce + "|" + validFrom + "|" + validUntilStr + "|" + maxUsage;

            // Signature can be in the JSON payload or in the request field
            String signature = req.getSignature();
            if ((signature == null || signature.isBlank()) && json.has("signature")) {
                signature = json.get("signature").asText();
            }

            if (signature == null || signature.isBlank()) {
                return saveEventAndReturn(ticketId, req, ValidationResult.INVALID_SIGNATURE, "Missing signature", productType);
            }

            if (!qrVerificationService.verifySignature(sigPayload, signature)) {
                return saveEventAndReturn(ticketId, req, ValidationResult.INVALID_SIGNATURE, "Invalid signature", productType);
            }

            LocalDateTime validUntil = LocalDateTime.parse(validUntilStr);
            // Use offline timestamp for comparison if provided
            LocalDateTime now = LocalDateTime.now();
            if (req.isOffline() && req.getOfflineTimestamp() != null && !req.getOfflineTimestamp().isBlank()) {
                try {
                    now = LocalDateTime.parse(req.getOfflineTimestamp());
                } catch (Exception ignored) {
                    // fallback to server time
                    now = LocalDateTime.now();
                }
            }

            if (now.isAfter(validUntil)) {
                return saveEventAndReturn(ticketId, req, ValidationResult.EXPIRED, "Ticket expired", productType);
            }

            if (!antiReplayService.acquireValidationLock(ticketId)) {
                return saveEventAndReturn(ticketId, req, ValidationResult.ALREADY_USED, "Concurrent validation in progress", productType);
            }

            try {
                if (antiReplayService.isNonceUsed(nonce)) {
                    return saveEventAndReturn(ticketId, req, ValidationResult.ALREADY_USED, "Nonce already used", productType);
                }

                Ticket ticket = ticketRepository.findById(ticketId).orElse(null);

                if (ticket == null || ticket.getStatus() != TicketStatus.ACTIVE) {
                    // Offline conflict: ticket was already used/revoked while agent was offline
                    if (req.isOffline() && ticket != null && ticket.getStatus() == TicketStatus.USED) {
                        FraudAlert alert = FraudAlert.builder()
                            .ticketId(ticketId)
                            .alertType(FraudAlertType.OFFLINE_CONFLICT)
                            .description("Offline validation received but ticket already USED. Terminal: " + req.getTerminalId())
                            .terminalId(req.getTerminalId())
                            .location(req.getLocation())
                            .build();
                        alert = fraudAlertRepository.save(alert);
                        fraudAlertSseService.publish(alert);
                        return saveEventAndReturn(ticketId, req, ValidationResult.OFFLINE_CONFLICT,
                            "Offline conflict: ticket already used", productType);
                    }
                    return saveEventAndReturn(ticketId, req, ValidationResult.ALREADY_USED,
                        ticket == null ? "Ticket not found" : "Ticket status: " + ticket.getStatus(), productType);
                }

                boolean isPass = "PASS".equals(productType) || ticket.getMaxUsage() >= 999;

                // For PASS: don't check/increment usage count (unlimited rides)
                if (!isPass) {
                    if (ticket.getUsageCount() >= ticket.getMaxUsage()) {
                        // Offline conflict for CARNET: usage exceeded via offline sync
                        if (req.isOffline()) {
                            FraudAlert alert = FraudAlert.builder()
                                .ticketId(ticketId)
                                .alertType(FraudAlertType.OFFLINE_CONFLICT)
                                .description("Offline validation: CARNET usage exceeded. Count: " + ticket.getUsageCount() + "/" + ticket.getMaxUsage())
                                .terminalId(req.getTerminalId())
                                .location(req.getLocation())
                                .build();
                            alert = fraudAlertRepository.save(alert);
                            fraudAlertSseService.publish(alert);
                            return saveEventAndReturn(ticketId, req, ValidationResult.OFFLINE_CONFLICT,
                                "Offline conflict: max usage exceeded", productType);
                        }
                        return saveEventAndReturn(ticketId, req, ValidationResult.ALREADY_USED, "Max usage reached", productType);
                    }

                    ticket.setUsageCount(ticket.getUsageCount() + 1);
                    if (ticket.getUsageCount() >= ticket.getMaxUsage()) {
                        ticket.setStatus(TicketStatus.USED);
                    }
                }

                antiReplayService.markNonceUsed(nonce, Duration.ofHours(48));
                ticketRepository.save(ticket);

                // Use offline timestamp for event if available
                LocalDateTime eventTimestamp = LocalDateTime.now();
                if (req.isOffline() && req.getOfflineTimestamp() != null && !req.getOfflineTimestamp().isBlank()) {
                    try { eventTimestamp = LocalDateTime.parse(req.getOfflineTimestamp()); } catch (Exception ignored) {}
                }

                ValidationEvent event = ValidationEvent.builder()
                    .ticketId(ticketId)
                    .terminalId(req.getTerminalId())
                    .location(req.getLocation())
                    .timestamp(eventTimestamp)
                    .offline(req.isOffline())
                    .result(ValidationResult.OK)
                    .build();
                validationEventRepository.save(event);

                checkTemporalCollision(ticketId, req.getLocation());

                antiReplayService.releaseValidationLock(ticketId);

                int remaining = isPass ? -1 : (ticket.getMaxUsage() - ticket.getUsageCount());
                return ValidationResponse.builder()
                    .result(ValidationResult.OK)
                    .ticketId(ticketId)
                    .message("Validation successful")
                    .remainingUsages(remaining)
                    .validUntil(validUntilStr)
                    .productType(productType)
                    .build();

            } catch (Exception e) {
                if (ticketId != null) {
                    antiReplayService.releaseValidationLock(ticketId);
                }
                throw e;
            }

        } catch (Exception e) {
            log.error("Validation error", e);
            return ValidationResponse.builder()
                .result(ValidationResult.INVALID_SIGNATURE)
                .ticketId(ticketId)
                .message("Validation error: " + e.getMessage())
                .build();
        }
    }

    private ValidationResponse saveEventAndReturn(Long ticketId, TicketScanRequest req,
                                                   ValidationResult result, String message, String productType) {
        LocalDateTime eventTimestamp = LocalDateTime.now();
        if (req.isOffline() && req.getOfflineTimestamp() != null && !req.getOfflineTimestamp().isBlank()) {
            try { eventTimestamp = LocalDateTime.parse(req.getOfflineTimestamp()); } catch (Exception ignored) {}
        }
        if (ticketId != null) {
            ValidationEvent event = ValidationEvent.builder()
                .ticketId(ticketId)
                .terminalId(req.getTerminalId())
                .location(req.getLocation())
                .timestamp(eventTimestamp)
                .offline(req.isOffline())
                .result(result)
                .build();
            validationEventRepository.save(event);
        }
        return ValidationResponse.builder()
            .result(result)
            .ticketId(ticketId)
            .message(message)
            .productType(productType != null ? productType : "")
            .build();
    }

    private void checkTemporalCollision(Long ticketId, String currentLocation) {
        if (currentLocation == null) return;
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(collisionWindowMinutes);
        List<ValidationEvent> recent = validationEventRepository
            .findByTicketIdAndTimestampAfter(ticketId, windowStart);
        boolean collision = recent.stream()
            .filter(e -> e.getResult() == ValidationResult.OK)
            .anyMatch(e -> e.getLocation() != null && !e.getLocation().equals(currentLocation));
        if (collision) {
            log.warn("TEMPORAL COLLISION DETECTED for ticket {}: validated at different locations within {} minutes",
                ticketId, collisionWindowMinutes);
            FraudAlert alert = FraudAlert.builder()
                .ticketId(ticketId)
                .alertType(FraudAlertType.TEMPORAL_COLLISION)
                .description("Ticket validated at different locations within " + collisionWindowMinutes + " minutes. Current: " + currentLocation)
                .location(currentLocation)
                .build();
            alert = fraudAlertRepository.save(alert);
            // Broadcast to SSE subscribers in real-time
            fraudAlertSseService.publish(alert);
        }
    }

    public List<FraudAlert> getRecentFraudAlerts() {
        return fraudAlertRepository.findTop100ByOrderByCreatedAtDesc();
    }

    public List<FraudAlert> getUnresolvedFraudAlerts() {
        return fraudAlertRepository.findByResolvedFalseOrderByCreatedAtDesc();
    }

    public FraudAlert resolveFraudAlert(Long id) {
        FraudAlert alert = fraudAlertRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("FraudAlert not found: " + id));
        alert.setResolved(true);
        return fraudAlertRepository.save(alert);
    }

    public long countUnresolvedAlerts() {
        return fraudAlertRepository.countByResolvedFalse();
    }
}
