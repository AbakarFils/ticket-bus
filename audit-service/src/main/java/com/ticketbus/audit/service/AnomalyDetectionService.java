package com.ticketbus.audit.service;

import com.ticketbus.common.domain.AuditAction;
import com.ticketbus.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final AuditLogRepository auditLogRepository;

    @Value("${audit.anomaly-detection.max-validations-per-hour:10}")
    private int maxValidationsPerHour;

    /**
     * Runs every 5 minutes. Checks for anomalous patterns in audit logs.
     */
    @Scheduled(fixedDelayString = "${audit.anomaly-detection.interval-seconds:300}000")
    public void detectAnomalies() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        // Check for abnormally high validation rate
        long validationCount = auditLogRepository.countByActionAndTimestampAfter(
            AuditAction.TICKET_VALIDATED, oneHourAgo);

        if (validationCount > maxValidationsPerHour * 10L) {
            log.warn("ANOMALY DETECTED: {} validations in the last hour (threshold: {})",
                validationCount, maxValidationsPerHour * 10);
        }

        // Check for high fraud alert rate
        long fraudCount = auditLogRepository.countByActionAndTimestampAfter(
            AuditAction.FRAUD_ALERT_CREATED, oneHourAgo);

        if (fraudCount > 5) {
            log.warn("ANOMALY DETECTED: {} fraud alerts in the last hour", fraudCount);
        }

        log.debug("Anomaly detection completed. Validations: {}, Fraud alerts: {}",
            validationCount, fraudCount);
    }

    public Map<String, Object> getAnomalyStats() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        Map<String, Object> stats = new HashMap<>();
        stats.put("validationsLastHour",
            auditLogRepository.countByActionAndTimestampAfter(AuditAction.TICKET_VALIDATED, oneHourAgo));
        stats.put("validationsLastDay",
            auditLogRepository.countByActionAndTimestampAfter(AuditAction.TICKET_VALIDATED, oneDayAgo));
        stats.put("fraudAlertsLastHour",
            auditLogRepository.countByActionAndTimestampAfter(AuditAction.FRAUD_ALERT_CREATED, oneHourAgo));
        stats.put("fraudAlertsLastDay",
            auditLogRepository.countByActionAndTimestampAfter(AuditAction.FRAUD_ALERT_CREATED, oneDayAgo));
        stats.put("paymentsLastDay",
            auditLogRepository.countByActionAndTimestampAfter(AuditAction.PAYMENT_INITIATED, oneDayAgo));
        stats.put("ticketsPurchasedLastDay",
            auditLogRepository.countByActionAndTimestampAfter(AuditAction.TICKET_PURCHASED, oneDayAgo));
        return stats;
    }
}

