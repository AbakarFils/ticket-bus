package com.ticketbus.validation.controller;

import com.ticketbus.common.domain.FraudAlert;
import com.ticketbus.common.domain.Ticket;
import com.ticketbus.common.domain.TicketStatus;
import com.ticketbus.common.domain.ValidationEvent;
import com.ticketbus.validation.dto.TicketScanRequest;
import com.ticketbus.validation.dto.ValidationResponse;
import com.ticketbus.validation.repository.TicketRepository;
import com.ticketbus.validation.repository.ValidationEventRepository;
import com.ticketbus.validation.service.FraudAlertSseService;
import com.ticketbus.validation.service.QrVerificationService;
import com.ticketbus.validation.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/validate")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;
    private final ValidationEventRepository validationEventRepository;
    private final FraudAlertSseService fraudAlertSseService;
    private final TicketRepository ticketRepository;
    private final QrVerificationService qrVerificationService;

    @PostMapping
    public ResponseEntity<ValidationResponse> validate(@RequestBody TicketScanRequest request) {
        return ResponseEntity.ok(validationService.validate(request));
    }

    /** Batch sync endpoint for offline events (agent app) */
    @PostMapping("/batch")
    public ResponseEntity<List<ValidationResponse>> validateBatch(@RequestBody List<TicketScanRequest> requests) {
        List<ValidationResponse> results = requests.stream()
            .map(validationService::validate)
            .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    /**
     * Sync endpoint for agent offline mode.
     * Returns: RSA public key, blacklisted ticket IDs, active ticket secrets for TOTP verification.
     */
    @GetMapping("/sync-data")
    public ResponseEntity<Map<String, Object>> getSyncData() {
        Map<String, Object> data = new HashMap<>();

        // RSA public key for local signature verification
        data.put("publicKey", qrVerificationService.getPublicKeyBase64());

        // Blacklisted ticket IDs (REVOKED, FRAUD, CANCELLED)
        List<Long> blacklist = ticketRepository.findByStatusIn(
            List.of(TicketStatus.REVOKED, TicketStatus.FRAUD, TicketStatus.CANCELLED)
        ).stream().map(Ticket::getId).collect(Collectors.toList());
        data.put("blacklistedTicketIds", blacklist);

        // Active ticket secrets for dynamic QR verification
        Map<Long, String> secrets = new HashMap<>();
        ticketRepository.findByStatus(TicketStatus.ACTIVE).forEach(t -> {
            if (t.getSecret() != null && !t.getSecret().isBlank()) {
                secrets.put(t.getId(), t.getSecret());
            }
        });
        data.put("ticketSecrets", secrets);

        data.put("syncTimestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(data);
    }

    @GetMapping("/events")
    public ResponseEntity<List<ValidationEvent>> getRecentEvents() {
        return ResponseEntity.ok(validationEventRepository.findTop100ByOrderByTimestampDesc());
    }

    @GetMapping("/events/ticket/{ticketId}")
    public ResponseEntity<List<ValidationEvent>> getEventsByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(validationEventRepository.findByTicketId(ticketId));
    }

    // --- Fraud Alerts ---

    @GetMapping("/fraud-alerts")
    public ResponseEntity<List<FraudAlert>> getFraudAlerts() {
        return ResponseEntity.ok(validationService.getRecentFraudAlerts());
    }

    @GetMapping("/fraud-alerts/unresolved")
    public ResponseEntity<List<FraudAlert>> getUnresolvedFraudAlerts() {
        return ResponseEntity.ok(validationService.getUnresolvedFraudAlerts());
    }

    @GetMapping("/fraud-alerts/count")
    public ResponseEntity<Map<String, Long>> countUnresolved() {
        return ResponseEntity.ok(Map.of("count", validationService.countUnresolvedAlerts()));
    }

    @PutMapping("/fraud-alerts/{id}/resolve")
    public ResponseEntity<FraudAlert> resolveFraudAlert(@PathVariable Long id) {
        return ResponseEntity.ok(validationService.resolveFraudAlert(id));
    }

    /** SSE endpoint for real-time fraud alerts */
    @GetMapping(value = "/fraud-alerts/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamFraudAlerts() {
        return fraudAlertSseService.subscribe();
    }
}
