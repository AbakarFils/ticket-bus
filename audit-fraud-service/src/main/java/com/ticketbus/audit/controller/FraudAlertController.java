package com.ticketbus.audit.controller;

import com.ticketbus.audit.domain.FraudAlert;
import com.ticketbus.audit.dto.CreateFraudAlertRequest;
import com.ticketbus.audit.service.FraudAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fraud/alerts")
@RequiredArgsConstructor
public class FraudAlertController {

    private final FraudAlertService fraudAlertService;

    @PostMapping
    public ResponseEntity<FraudAlert> createAlert(@RequestBody CreateFraudAlertRequest request) {
        return ResponseEntity.ok(fraudAlertService.createAlert(request));
    }

    @GetMapping
    public ResponseEntity<List<FraudAlert>> getActiveAlerts() {
        return ResponseEntity.ok(fraudAlertService.getActiveAlerts());
    }

    @GetMapping("/all")
    public ResponseEntity<List<FraudAlert>> getAllAlerts() {
        return ResponseEntity.ok(fraudAlertService.getAllAlerts());
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveAlert(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(fraudAlertService.resolveAlert(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countActiveAlerts() {
        return ResponseEntity.ok(Map.of("count", fraudAlertService.countActiveAlerts()));
    }
}
