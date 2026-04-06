package com.ticketbus.audit.repository;

import com.ticketbus.audit.domain.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByResolvedFalseOrderByCreatedAtDesc();
    List<FraudAlert> findAllByOrderByCreatedAtDesc();
    List<FraudAlert> findByTicketId(Long ticketId);
    long countByResolvedFalse();
}
