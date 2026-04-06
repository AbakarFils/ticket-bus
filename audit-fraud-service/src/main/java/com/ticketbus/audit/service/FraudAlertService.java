package com.ticketbus.audit.service;

import com.ticketbus.audit.domain.AuditLog;
import com.ticketbus.audit.domain.BlacklistedTicket;
import com.ticketbus.audit.domain.FraudAlert;
import com.ticketbus.audit.dto.BlacklistRequest;
import com.ticketbus.audit.dto.CreateFraudAlertRequest;
import com.ticketbus.audit.repository.AuditLogRepository;
import com.ticketbus.audit.repository.BlacklistedTicketRepository;
import com.ticketbus.audit.repository.FraudAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudAlertService {

    private final FraudAlertRepository fraudAlertRepository;
    private final BlacklistedTicketRepository blacklistedTicketRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public FraudAlert createAlert(CreateFraudAlertRequest req) {
        FraudAlert alert = FraudAlert.builder()
            .ticketId(req.ticketId())
            .userId(req.userId())
            .terminalId(req.terminalId())
            .alertType(req.alertType())
            .description(req.description())
            .build();
        log.warn("Fraud alert created: type={}, ticketId={}", req.alertType(), req.ticketId());
        return fraudAlertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public List<FraudAlert> getActiveAlerts() {
        return fraudAlertRepository.findByResolvedFalseOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<FraudAlert> getAllAlerts() {
        return fraudAlertRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public FraudAlert resolveAlert(Long id) {
        FraudAlert alert = fraudAlertRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));
        alert.setResolved(true);
        return fraudAlertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public long countActiveAlerts() {
        return fraudAlertRepository.countByResolvedFalse();
    }

    @Transactional
    public BlacklistedTicket blacklistTicket(BlacklistRequest req) {
        if (blacklistedTicketRepository.existsByTicketId(req.ticketId())) {
            throw new IllegalArgumentException("Ticket already blacklisted: " + req.ticketId());
        }
        BlacklistedTicket bt = BlacklistedTicket.builder()
            .ticketId(req.ticketId())
            .reason(req.reason())
            .blacklistedBy(req.blacklistedBy())
            .build();
        bt = blacklistedTicketRepository.save(bt);
        logAction("TICKET", req.ticketId().toString(), "BLACKLISTED",
            req.blacklistedBy(), req.reason());
        return bt;
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(Long ticketId) {
        return blacklistedTicketRepository.existsByTicketId(ticketId);
    }

    @Transactional
    public void removeFromBlacklist(Long ticketId) {
        blacklistedTicketRepository.deleteByTicketId(ticketId);
        logAction("TICKET", ticketId.toString(), "UNBLACKLISTED", "system", "Removed from blacklist");
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findTop200ByOrderByTimestampDesc();
    }

    @Transactional
    public void logAction(String entityType, String entityId, String action,
                          String performedBy, String details) {
        auditLogRepository.save(AuditLog.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action(action)
            .performedBy(performedBy)
            .details(details)
            .build());
    }
}
