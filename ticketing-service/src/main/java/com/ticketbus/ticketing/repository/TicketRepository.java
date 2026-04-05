package com.ticketbus.ticketing.repository;

import com.ticketbus.common.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByNonce(String nonce);
    List<Ticket> findByUserId(Long userId);
}
