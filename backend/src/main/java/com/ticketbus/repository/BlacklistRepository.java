package com.ticketbus.repository;

import com.ticketbus.entity.BlacklistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlacklistRepository extends JpaRepository<BlacklistEntry, UUID> {
    Optional<BlacklistEntry> findByTicketNumberAndActiveTrue(String ticketNumber);
    List<BlacklistEntry> findByActiveTrue();
    List<BlacklistEntry> findByBlacklistedAtAfter(LocalDateTime after);
}
