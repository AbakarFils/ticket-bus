package com.ticketbus.common.audit;

import com.ticketbus.common.domain.AuditAction;
import com.ticketbus.common.domain.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Lightweight audit client for fire-and-forget audit logging.
 * Each service injects this to send audit events to the audit-service.
 */
@Slf4j
@Component
public class AuditClient {

    private final RestTemplate restTemplate;
    private final String auditUrl;
    private final String serviceName;

    public AuditClient(
            @Value("${services.audit-url:http://localhost:8087}") String auditUrl,
            @Value("${spring.application.name:unknown}") String serviceName) {
        this.restTemplate = new RestTemplate();
        this.auditUrl = auditUrl;
        this.serviceName = serviceName;
    }

    public void log(AuditAction action, String entityType, Long entityId, Long userId, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .serviceName(serviceName)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .details(details)
                .build();
            restTemplate.postForEntity(auditUrl + "/api/audit/logs", new HttpEntity<>(auditLog), Void.class);
        } catch (Exception e) {
            // Fire and forget — do not break the caller
            log.debug("Failed to send audit log to audit-service: {}", e.getMessage());
        }
    }
}

