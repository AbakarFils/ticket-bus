package com.ticketbus.audit.repository;

import com.ticketbus.audit.domain.BlacklistedTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BlacklistedTicketRepository extends JpaRepository<BlacklistedTicket, Long> {
    Optional<BlacklistedTicket> findByTicketId(Long ticketId);
    boolean existsByTicketId(Long ticketId);
    void deleteByTicketId(Long ticketId);
}
