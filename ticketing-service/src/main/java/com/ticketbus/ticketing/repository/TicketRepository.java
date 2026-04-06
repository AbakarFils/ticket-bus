package com.ticketbus.ticketing.repository;

import com.ticketbus.common.domain.Ticket;
import com.ticketbus.common.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByNonce(String nonce);
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findTop100ByOrderByCreatedAtDesc();
    List<Ticket> findByStatus(TicketStatus status);
}
