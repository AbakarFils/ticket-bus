package com.ticketbus.repository;

import com.ticketbus.entity.ValidationEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ValidationEventRepository extends JpaRepository<ValidationEvent, UUID> {
    List<ValidationEvent> findByValidatorDeviceIdAndSyncedFalse(String deviceId);
    Page<ValidationEvent> findByTicketId(UUID ticketId, Pageable pageable);
    List<ValidationEvent> findByTicketTicketNumberAndValidationTimeAfter(String ticketNumber, LocalDateTime after);
}
