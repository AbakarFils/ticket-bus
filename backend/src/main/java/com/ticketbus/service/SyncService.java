package com.ticketbus.service;

import com.ticketbus.dto.*;
import com.ticketbus.entity.BlacklistEntry;
import com.ticketbus.entity.ValidationEvent;
import com.ticketbus.entity.ValidationStatus;
import com.ticketbus.repository.BlacklistRepository;
import com.ticketbus.repository.ValidationEventRepository;
import com.ticketbus.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final BlacklistRepository blacklistRepository;
    private final ValidationEventRepository validationEventRepository;
    private final TicketRepository ticketRepository;
    private final CryptoService cryptoService;

    @Transactional
    public SyncResponse syncValidationEvents(SyncRequest req) {
        int processedCount = 0;
        if (req.validationEvents() != null) {
            for (ValidationEventDTO dto : req.validationEvents()) {
                try {
                    ticketRepository.findByTicketNumber(dto.ticketNumber()).ifPresent(ticket -> {
                        ValidationEvent event = ValidationEvent.builder()
                                .ticket(ticket)
                                .validatorDeviceId(dto.deviceId())
                                .validationTime(dto.validationTime())
                                .location(dto.location())
                                .latitude(dto.latitude())
                                .longitude(dto.longitude())
                                .status(ValidationStatus.OFFLINE_PENDING)
                                .synced(true)
                                .syncedAt(LocalDateTime.now())
                                .build();
                        validationEventRepository.save(event);
                    });
                    processedCount++;
                } catch (Exception e) {
                    log.warn("Failed to process sync event for ticket {}: {}", dto.ticketNumber(), e.getMessage());
                }
            }
        }

        List<BlacklistDTO> blacklistUpdates = getBlacklistUpdates(req.lastSyncTime());
        PublicKeyDTO publicKey = getPublicKeyUpdate();

        return new SyncResponse(blacklistUpdates, publicKey, processedCount);
    }

    public List<BlacklistDTO> getBlacklistUpdates(LocalDateTime since) {
        List<BlacklistEntry> entries = since != null
                ? blacklistRepository.findByBlacklistedAtAfter(since)
                : blacklistRepository.findByActiveTrue();
        return entries.stream()
                .map(e -> new BlacklistDTO(e.getId(), e.getTicketNumber(), e.getReason(), e.getBlacklistedAt(), e.isActive()))
                .collect(Collectors.toList());
    }

    public PublicKeyDTO getPublicKeyUpdate() {
        var keyPair = cryptoService.getActiveKeyPair();
        return new PublicKeyDTO(keyPair.getPublicKey(), keyPair.getAlgorithm(), keyPair.getCreatedAt());
    }
}
