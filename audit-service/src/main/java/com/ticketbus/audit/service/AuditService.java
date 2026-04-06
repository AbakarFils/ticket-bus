package com.ticketbus.audit.service;

import com.ticketbus.common.domain.AuditAction;
import com.ticketbus.common.domain.AuditLog;
import com.ticketbus.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} - {} - entity {}",
                auditLog.getServiceName(), auditLog.getAction(), auditLog.getEntityId());
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop200ByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsByService(String serviceName) {
        return auditLogRepository.findByServiceNameOrderByTimestampDesc(serviceName);
    }

    public List<AuditLog> getLogsByAction(AuditAction action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }

    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<AuditLog> getLogsByDateRange(LocalDateTime from, LocalDateTime to) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(from, to);
    }
}

