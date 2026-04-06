package com.ticketbus.service;

import com.ticketbus.entity.BlacklistEntry;
import com.ticketbus.entity.ValidationEvent;
import com.ticketbus.entity.ValidationStatus;
import com.ticketbus.repository.BlacklistRepository;
import com.ticketbus.repository.ValidationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AntiFraudService {

    private final BlacklistRepository blacklistRepository;
    private final ValidationEventRepository validationEventRepository;

    public boolean isBlacklisted(String ticketNumber) {
        return blacklistRepository.findByTicketNumberAndActiveTrue(ticketNumber).isPresent();
    }

    public boolean checkReplayAttack(String nonce, String ticketNumber) {
        List<ValidationEvent> recentEvents = validationEventRepository
                .findByTicketTicketNumberAndValidationTimeAfter(ticketNumber, LocalDateTime.now().minusHours(24));
        return recentEvents.stream().anyMatch(e -> e.getStatus() == ValidationStatus.VALID);
    }

    public boolean checkTemporalCollision(String ticketNumber, LocalDateTime time, String location) {
        List<ValidationEvent> recentEvents = validationEventRepository
                .findByTicketTicketNumberAndValidationTimeAfter(ticketNumber, LocalDateTime.now().minusMinutes(5));
        return recentEvents.stream()
                .filter(e -> e.getStatus() == ValidationStatus.VALID)
                .anyMatch(e -> e.getLocation() != null && !e.getLocation().equals(location));
    }

    @Transactional
    public void flagSuspiciousActivity(UUID ticketId, String reason) {
        log.warn("Suspicious activity detected for ticket ID: {} - {}", ticketId, reason);
    }

    @Transactional
    public void addToBlacklist(String ticketNumber, String reason) {
        BlacklistEntry entry = BlacklistEntry.builder()
                .ticketNumber(ticketNumber)
                .reason(reason)
                .blacklistedAt(LocalDateTime.now())
                .active(true)
                .build();
        blacklistRepository.save(entry);
        log.info("Ticket {} added to blacklist: {}", ticketNumber, reason);
    }
}
