package com.ticketbus.ticketing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbus.common.domain.Ticket;
import com.ticketbus.common.domain.TicketStatus;
import com.ticketbus.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrValidationService {

    private final TicketRepository ticketRepository;
    private final QrSigningService qrSigningService;
    private final DynamicQrService dynamicQrService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class ValidationResult {
        public final boolean valid;
        public final String reason;
        public final Ticket ticket;
        public final String ticketInfo;

        public ValidationResult(boolean valid, String reason, Ticket ticket, String ticketInfo) {
            this.valid = valid;
            this.reason = reason;
            this.ticket = ticket;
            this.ticketInfo = ticketInfo;
        }

        public static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason, null, null);
        }

        public static ValidationResult valid(Ticket ticket, String info) {
            return new ValidationResult(true, "Ticket valide", ticket, info);
        }
    }

    /**
     * Valide un QR code scanné et retourne le résultat de validation
     */
    public ValidationResult validateQrCode(String qrPayload) {
        try {
            log.info("Validating QR payload: {}", qrPayload);

            // Parse QR payload JSON
            Map<String, Object> qrData = objectMapper.readValue(qrPayload, Map.class);

            Long ticketId = Long.valueOf(qrData.get("ticketId").toString());
            String signature = (String) qrData.get("signature");

            if (signature == null) {
                return ValidationResult.invalid("QR code manquant signature");
            }

            // Find ticket in database
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return ValidationResult.invalid("Ticket introuvable");
            }

            // Check ticket status
            if (ticket.getStatus() != TicketStatus.ACTIVE) {
                return ValidationResult.invalid("Ticket inactif (" + ticket.getStatus() + ")");
            }

            // Check expiry
            LocalDateTime now = LocalDateTime.now();
            if (ticket.getValidUntil() != null && now.isAfter(ticket.getValidUntil())) {
                return ValidationResult.invalid("Ticket expiré");
            }

            // Check not valid yet
            if (ticket.getValidFrom() != null && now.isBefore(ticket.getValidFrom())) {
                return ValidationResult.invalid("Ticket pas encore valide");
            }

            // Check usage limit
            if (ticket.getMaxUsage() < 999 && ticket.getUsageCount() >= ticket.getMaxUsage()) {
                return ValidationResult.invalid("Nombre maximum d'usages atteint");
            }

            // Validate signature
            boolean isValidSignature = validateSignature(qrData, ticket);
            if (!isValidSignature) {
                return ValidationResult.invalid("Signature QR code invalide");
            }

            // Build ticket info
            String ticketInfo = String.format("Ticket #%d - Reste %s usage(s)",
                ticket.getId(),
                ticket.getMaxUsage() >= 999 ? "∞" : String.valueOf(ticket.getMaxUsage() - ticket.getUsageCount()));

            return ValidationResult.valid(ticket, ticketInfo);

        } catch (Exception e) {
            log.error("Error validating QR code", e);
            return ValidationResult.invalid("Erreur lors de la validation: " + e.getMessage());
        }
    }

    /**
     * Marque un ticket comme utilisé (incrémente le compteur d'usage)
     */
    public boolean useTicket(Long ticketId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return false;
            }

            if (ticket.getMaxUsage() < 999 && ticket.getUsageCount() >= ticket.getMaxUsage()) {
                return false; // Already at max usage
            }

            ticket.setUsageCount(ticket.getUsageCount() + 1);

            // If this was the last usage, mark as USED
            if (ticket.getMaxUsage() < 999 && ticket.getUsageCount() >= ticket.getMaxUsage()) {
                ticket.setStatus(TicketStatus.USED);
            }

            ticketRepository.save(ticket);
            log.info("Ticket {} usage incremented to {}/{}", ticketId, ticket.getUsageCount(), ticket.getMaxUsage());
            return true;

        } catch (Exception e) {
            log.error("Error using ticket {}", ticketId, e);
            return false;
        }
    }

    private boolean validateSignature(Map<String, Object> qrData, Ticket ticket) {
        try {
            String signature = (String) qrData.get("signature");

            // Check if this is a dynamic QR (has liveNonce)
            if (qrData.containsKey("liveNonce")) {
                // Validate dynamic QR
                return dynamicQrService.validateDynamicQr(qrData, ticket);
            } else {
                // Validate static QR - rebuild payload and verify signature
                String rebuiltPayload = qrSigningService.buildPayload(ticket);
                return qrSigningService.verify(rebuiltPayload, signature);
            }
        } catch (Exception e) {
            log.error("Error validating signature", e);
            return false;
        }
    }
}
