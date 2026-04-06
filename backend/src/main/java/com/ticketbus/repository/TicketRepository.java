package com.ticketbus.repository;

import com.ticketbus.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByTicketNumber(String ticketNumber);
}
