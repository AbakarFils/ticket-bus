package com.ticketbus.validation.service;

import com.ticketbus.validation.client.AuditFraudClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbus.common.domain.*;
import com.ticketbus.validation.dto.TicketScanRequest;
import com.ticketbus.validation.dto.ValidationResponse;
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
    private final AuditFraudClient auditFraudClient;
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

            String userId = json.get("userId").asText();
            String validFrom = json.get("validFrom").asText();
            String sigPayload = ticketId + "|" + userId + "|" + nonce + "|" + validFrom + "|" + validUntilStr + "|" + maxUsage;

            if (!qrVerificationService.verifySignature(sigPayload, req.getSignature())) {
                return saveEventAndReturn(ticketId, req, ValidationResult.INVALID_SIGNATURE, "Invalid signature");
            }

            if (auditFraudClient.isTicketBlacklisted(ticketId)) {
                return saveEventAndReturn(ticketId, req, ValidationResult.BLACKLISTED, "Ticket is blacklisted");
            }

            LocalDateTime validUntil = LocalDateTime.parse(validUntilStr);
            if (LocalDateTime.now().isAfter(validUntil)) {
                return saveEventAndReturn(ticketId, req, ValidationResult.EXPIRED, "Ticket expired");
            }

            if (!antiReplayService.acquireValidationLock(ticketId)) {
                return saveEventAndReturn(ticketId, req, ValidationResult.ALREADY_USED, "Concurrent validation in progress");
            }

            try {
                if (antiReplayService.isNonceUsed(nonce)) {
                    return saveEventAndReturn(ticketId, req, ValidationResult.ALREADY_USED, "Nonce already used");
                }

                Ticket ticket = ticketRepository.findById(ticketId).orElse(null);

                if (ticket == null || ticket.getStatus() != TicketStatus.ACTIVE) {
                    return saveEventAndReturn(ticketId, req, ValidationResult.ALREADY_USED,
                        ticket == null ? "Ticket not found" : "Ticket status: " + ticket.getStatus());
                }

                if (ticket.getUsageCount() >= ticket.getMaxUsage()) {
                    return saveEventAndReturn(ticketId, req, ValidationResult.ALREADY_USED, "Max usage reached");
                }

                antiReplayService.markNonceUsed(nonce, Duration.ofHours(48));

                ticket.setUsageCount(ticket.getUsageCount() + 1);
                if (ticket.getUsageCount() >= ticket.getMaxUsage()) {
                    ticket.setStatus(TicketStatus.USED);
                }

                ticketRepository.save(ticket);

                ValidationEvent event = ValidationEvent.builder()
                    .ticketId(ticketId)
                    .terminalId(req.getTerminalId())
                    .location(req.getLocation())
                    .timestamp(LocalDateTime.now())
                    .offline(req.isOffline())
                    .result(ValidationResult.OK)
                    .build();
                validationEventRepository.save(event);

                checkTemporalCollision(ticketId, req.getLocation());

                antiReplayService.releaseValidationLock(ticketId);

                return ValidationResponse.builder()
                    .result(ValidationResult.OK)
                    .ticketId(ticketId)
                    .message("Validation successful")
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
                                                   ValidationResult result, String message) {
        if (ticketId != null) {
            ValidationEvent event = ValidationEvent.builder()
                .ticketId(ticketId)
                .terminalId(req.getTerminalId())
                .location(req.getLocation())
                .timestamp(LocalDateTime.now())
                .offline(req.isOffline())
                .result(result)
                .build();
            validationEventRepository.save(event);
        }
        return ValidationResponse.builder()
            .result(result)
            .ticketId(ticketId)
            .message(message)
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
            auditFraudClient.reportFraudAlert(ticketId, null, null, "DOUBLE_SCAN",
                "Ticket validated at different locations within " + collisionWindowMinutes + " minutes");
        }
    }
}
