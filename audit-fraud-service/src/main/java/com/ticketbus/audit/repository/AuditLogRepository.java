package com.ticketbus.audit.repository;

import com.ticketbus.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop200ByOrderByTimestampDesc();
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);
}
