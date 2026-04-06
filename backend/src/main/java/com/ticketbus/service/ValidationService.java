package com.ticketbus.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbus.dto.TicketResponse;
import com.ticketbus.dto.ValidationRequest;
import com.ticketbus.dto.ValidationResponse;
import com.ticketbus.entity.*;
import com.ticketbus.repository.TicketRepository;
import com.ticketbus.repository.ValidationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final CryptoService cryptoService;
    private final TicketRepository ticketRepository;
    private final ValidationEventRepository validationEventRepository;
    private final AntiFraudService antiFraudService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ValidationResponse validateTicket(ValidationRequest req) {
        LocalDateTime validationTime = req.validationTime() != null ? req.validationTime() : LocalDateTime.now();

        Map<String, Object> qrData;
        try {
            qrData = objectMapper.readValue(req.qrCodeData(), new TypeReference<>() {});
        } catch (Exception e) {
            return saveAndReturn(null, req, ValidationStatus.INVALID, "Invalid QR code format", false);
        }

        String signatureB64 = (String) qrData.get("signature");
        if (signatureB64 == null) {
            return saveAndReturn(null, req, ValidationStatus.INVALID, "Missing signature", false);
        }

        Map<String, Object> payloadWithoutSig = new LinkedHashMap<>(qrData);
        payloadWithoutSig.remove("signature");

        String dataToVerify;
        try {
            dataToVerify = objectMapper.writeValueAsString(payloadWithoutSig);
        } catch (Exception e) {
            return saveAndReturn(null, req, ValidationStatus.INVALID, "Failed to process QR payload", false);
        }

        if (!cryptoService.verifySignature(dataToVerify, signatureB64)) {
            return saveAndReturn(null, req, ValidationStatus.INVALID, "Invalid signature", false);
        }

        String ticketNumber = (String) qrData.get("ticketNumber");
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketNumber(ticketNumber);
        if (ticketOpt.isEmpty()) {
            return saveAndReturn(null, req, ValidationStatus.INVALID, "Ticket not found", false);
        }
        Ticket ticket = ticketOpt.get();

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            return saveAndReturn(ticket, req, ValidationStatus.INVALID, "Ticket is not active: " + ticket.getStatus(), false);
        }

        if (ticket.getActivationWindowStart() != null && validationTime.isBefore(ticket.getActivationWindowStart())) {
            return saveAndReturn(ticket, req, ValidationStatus.INVALID, "Ticket not yet valid", false);
        }
        if (ticket.getActivationWindowEnd() != null && validationTime.isAfter(ticket.getActivationWindowEnd())) {
            return saveAndReturn(ticket, req, ValidationStatus.INVALID, "Ticket has expired", false);
        }

        if (antiFraudService.isBlacklisted(ticketNumber)) {
            return saveAndReturn(ticket, req, ValidationStatus.INVALID, "Ticket is blacklisted", false);
        }

        String nonce = (String) qrData.get("nonce");
        if (antiFraudService.checkReplayAttack(nonce, ticketNumber)) {
            saveValidationEvent(ticket, req, ValidationStatus.SUSPECT, "Replay attack detected", validationTime);
            antiFraudService.flagSuspiciousActivity(ticket.getId(), "Replay attack detected");
            return new ValidationResponse(false, ValidationStatus.SUSPECT, "Suspicious activity detected", toResponse(ticket));
        }

        if (ticket.getUsageCount() >= ticket.getMaxUsageCount()) {
            return saveAndReturn(ticket, req, ValidationStatus.INVALID, "Ticket usage limit exceeded", false);
        }

        if (antiFraudService.checkTemporalCollision(ticketNumber, validationTime, req.location())) {
            saveValidationEvent(ticket, req, ValidationStatus.SUSPECT, "Temporal collision detected", validationTime);
            antiFraudService.flagSuspiciousActivity(ticket.getId(), "Temporal collision detected");
            return new ValidationResponse(false, ValidationStatus.SUSPECT, "Suspicious activity detected", toResponse(ticket));
        }

        ticket.setUsageCount(ticket.getUsageCount() + 1);
        if (ticket.getUsageCount() >= ticket.getMaxUsageCount()) {
            ticket.setStatus(TicketStatus.USED);
        }
        ticketRepository.save(ticket);

        saveValidationEvent(ticket, req, ValidationStatus.VALID, null, validationTime);
        return new ValidationResponse(true, ValidationStatus.VALID, "Ticket validated successfully", toResponse(ticket));
    }

    private ValidationResponse saveAndReturn(Ticket ticket, ValidationRequest req, ValidationStatus status, String reason, boolean valid) {
        LocalDateTime validationTime = req.validationTime() != null ? req.validationTime() : LocalDateTime.now();
        saveValidationEvent(ticket, req, status, reason, validationTime);
        TicketResponse ticketResponse = ticket != null ? toResponse(ticket) : null;
        return new ValidationResponse(valid, status, reason, ticketResponse);
    }

    private void saveValidationEvent(Ticket ticket, ValidationRequest req, ValidationStatus status, String reason, LocalDateTime validationTime) {
        ValidationEvent event = ValidationEvent.builder()
                .ticket(ticket)
                .validatorDeviceId(req.deviceId())
                .validationTime(validationTime)
                .location(req.location())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .status(status)
                .rejectionReason(reason)
                .synced(status == ValidationStatus.VALID)
                .build();
        validationEventRepository.save(event);
    }

    public Page<ValidationEvent> listEvents(Pageable pageable) {
        return validationEventRepository.findAll(pageable);
    }

    public Page<ValidationEvent> getEventsForTicket(UUID ticketId, Pageable pageable) {
        return validationEventRepository.findByTicketId(ticketId, pageable);
    }

    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(), t.getTicketNumber(), t.getPassengerName(), t.getPassengerEmail(),
                t.getRouteName(), t.getDepartureLocation(), t.getArrivalLocation(),
                t.getDepartureTime(), t.getArrivalTime(), t.getPrice(), t.getStatus(),
                t.getNonce(), t.getUsageCount(), t.getMaxUsageCount(),
                t.getActivationWindowStart(), t.getActivationWindowEnd(),
                t.getQrCodeData(), t.getCreatedAt(), t.getUpdatedAt()
        );
    }
}
