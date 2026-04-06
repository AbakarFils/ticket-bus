package com.ticketbus.audit.controller;

import com.ticketbus.common.domain.AuditAction;
import com.ticketbus.common.domain.AuditLog;
import com.ticketbus.audit.service.AnomalyDetectionService;
import com.ticketbus.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final AnomalyDetectionService anomalyDetectionService;

    /** Ingest audit events from other services */
    @PostMapping("/logs")
    public ResponseEntity<Void> ingest(@RequestBody AuditLog auditLog) {
        auditService.log(auditLog);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getRecentLogs() {
        return ResponseEntity.ok(auditService.getRecentLogs());
    }

    @GetMapping("/logs/service/{serviceName}")
    public ResponseEntity<List<AuditLog>> getByService(@PathVariable String serviceName) {
        return ResponseEntity.ok(auditService.getLogsByService(serviceName));
    }

    @GetMapping("/logs/action/{action}")
    public ResponseEntity<List<AuditLog>> getByAction(@PathVariable AuditAction action) {
        return ResponseEntity.ok(auditService.getLogsByAction(action));
    }

    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<List<AuditLog>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditService.getLogsByUser(userId));
    }

    @GetMapping("/anomalies/stats")
    public ResponseEntity<Map<String, Object>> getAnomalyStats() {
        return ResponseEntity.ok(anomalyDetectionService.getAnomalyStats());
    }
}

