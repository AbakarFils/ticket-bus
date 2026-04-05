package com.ticketbus.validation.repository;

import com.ticketbus.common.domain.ValidationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ValidationEventRepository extends JpaRepository<ValidationEvent, Long> {
    List<ValidationEvent> findByTicketIdAndTimestampAfter(Long ticketId, LocalDateTime after);
    List<ValidationEvent> findByTicketId(Long ticketId);
    List<ValidationEvent> findTop100ByOrderByTimestampDesc();
}
