package com.ticketbus.audit.controller;

import com.ticketbus.audit.domain.AuditLog;
import com.ticketbus.audit.service.FraudAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final FraudAlertService fraudAlertService;

    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(fraudAlertService.getAuditLogs());
    }

    @PostMapping("/logs")
    public ResponseEntity<Map<String, String>> logAction(@RequestBody Map<String, String> body) {
        fraudAlertService.logAction(
            body.getOrDefault("entityType", "UNKNOWN"),
            body.getOrDefault("entityId", ""),
            body.getOrDefault("action", ""),
            body.getOrDefault("performedBy", "system"),
            body.getOrDefault("details", "")
        );
        return ResponseEntity.ok(Map.of("message", "Log recorded"));
    }
}
