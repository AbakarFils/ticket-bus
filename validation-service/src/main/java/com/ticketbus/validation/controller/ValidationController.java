package com.ticketbus.validation.controller;

import com.ticketbus.common.domain.FraudAlert;
import com.ticketbus.common.domain.ValidationEvent;
import com.ticketbus.validation.dto.TicketScanRequest;
import com.ticketbus.validation.dto.ValidationResponse;
import com.ticketbus.validation.repository.ValidationEventRepository;
import com.ticketbus.validation.service.FraudAlertSseService;
import com.ticketbus.validation.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @PostMapping
    public ResponseEntity<ValidationResponse> validate(@RequestBody TicketScanRequest request) {
        return ResponseEntity.ok(validationService.validate(request));
    }

    /** Batch sync endpoint for offline events (Telpo agent app) */
    @PostMapping("/batch")
    public ResponseEntity<List<ValidationResponse>> validateBatch(@RequestBody List<TicketScanRequest> requests) {
        List<ValidationResponse> results = requests.stream()
            .map(validationService::validate)
            .collect(Collectors.toList());
        return ResponseEntity.ok(results);
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
