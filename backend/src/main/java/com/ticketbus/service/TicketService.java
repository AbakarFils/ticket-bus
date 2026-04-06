package com.ticketbus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ticketbus.dto.TicketRequest;
import com.ticketbus.dto.TicketResponse;
import com.ticketbus.entity.Ticket;
import com.ticketbus.entity.TicketStatus;
import com.ticketbus.exception.ResourceNotFoundException;
import com.ticketbus.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;

    @Transactional
    public TicketResponse createTicket(TicketRequest req) {
        try {
            String ticketNumber = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            UUID nonce = UUID.randomUUID();

            Map<String, Object> payloadMap = new LinkedHashMap<>();
            payloadMap.put("ticketNumber", ticketNumber);
            payloadMap.put("nonce", nonce.toString());
            payloadMap.put("timestamp", LocalDateTime.now().toString());
            payloadMap.put("passenger", req.passengerName());
            payloadMap.put("route", req.routeName());
            payloadMap.put("departure", req.departureTime() != null ? req.departureTime().toString() : null);
            payloadMap.put("arrival", req.arrivalTime() != null ? req.arrivalTime().toString() : null);
            payloadMap.put("validFrom", req.activationWindowStart() != null ? req.activationWindowStart().toString() : null);
            payloadMap.put("validUntil", req.activationWindowEnd() != null ? req.activationWindowEnd().toString() : null);

            String dataToSign = objectMapper.writeValueAsString(payloadMap);
            String signature = cryptoService.signData(dataToSign);

            payloadMap.put("signature", signature);
            String qrPayload = objectMapper.writeValueAsString(payloadMap);

            Ticket ticket = Ticket.builder()
                    .ticketNumber(ticketNumber)
                    .passengerName(req.passengerName())
                    .passengerEmail(req.passengerEmail())
                    .routeName(req.routeName())
                    .departureLocation(req.departureLocation())
                    .arrivalLocation(req.arrivalLocation())
                    .departureTime(req.departureTime())
                    .arrivalTime(req.arrivalTime())
                    .price(req.price())
                    .nonce(nonce)
                    .maxUsageCount(req.maxUsageCount() > 0 ? req.maxUsageCount() : 1)
                    .activationWindowStart(req.activationWindowStart())
                    .activationWindowEnd(req.activationWindowEnd())
                    .qrCodeData(qrPayload)
                    .build();

            Ticket saved = ticketRepository.save(ticket);
            return toResponse(saved);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ticket: " + e.getMessage(), e);
        }
    }

    public TicketResponse getTicket(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
        return toResponse(ticket);
    }

    public TicketResponse getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketNumber));
        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse cancelTicket(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
        ticket.setStatus(TicketStatus.CANCELLED);
        return toResponse(ticketRepository.save(ticket));
    }

    public Page<TicketResponse> listTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::toResponse);
    }

    public byte[] generateQrCodeImage(String qrData) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 300, 300, hints);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
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
