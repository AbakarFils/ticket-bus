package com.ticketbus.audit.repository;

import com.ticketbus.common.domain.AuditAction;
import com.ticketbus.common.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop200ByOrderByTimestampDesc();
    List<AuditLog> findByServiceNameOrderByTimestampDesc(String serviceName);
    List<AuditLog> findByActionOrderByTimestampDesc(AuditAction action);
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime from, LocalDateTime to);

    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.entityId = :entityId AND a.timestamp > :since")
    List<AuditLog> findRecentByActionAndEntity(AuditAction action, Long entityId, LocalDateTime since);

    long countByActionAndTimestampAfter(AuditAction action, LocalDateTime since);
}

