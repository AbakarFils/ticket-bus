package com.ticketbus.ticketing.service;

import com.ticketbus.common.domain.*;
import com.ticketbus.common.dto.TicketDto;
import com.ticketbus.ticketing.client.WalletClient;
import com.ticketbus.ticketing.repository.ProductRepository;
import com.ticketbus.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketingService {

    private final TicketRepository ticketRepository;
    private final ProductRepository productRepository;
    private final QrSigningService qrSigningService;
    private final WalletClient walletClient;

    @Value("${ticketing.ticket.validity-minutes:60}")
    private int validityMinutes;

    @Transactional
    public Ticket purchaseTicket(Long userId, Long productId) throws Exception {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (!product.isActive()) {
            throw new IllegalArgumentException("Product is not active: " + productId);
        }

        BigDecimal balance = walletClient.getBalance(userId);
        if (balance.compareTo(product.getPrice()) < 0) {
            throw new IllegalStateException("Insufficient wallet balance. Required: " + product.getPrice() + ", Available: " + balance);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validUntil;
        if (product.getDurationDays() != null) {
            validUntil = now.plusDays(product.getDurationDays());
        } else {
            validUntil = now.plusMinutes(validityMinutes);
        }

        Ticket ticket = Ticket.builder()
            .userId(userId)
            .productId(productId)
            .nonce(UUID.randomUUID().toString())
            .secret(UUID.randomUUID().toString()) // Secret for dynamic QR rotation
            .validFrom(now)
            .validUntil(validUntil)
            .maxUsage(product.getMaxUsage())
            .operatorId(product.getOperatorId())
            .zone(product.getZoneCode())
            .status(TicketStatus.ACTIVE)
            .build();

        ticket = ticketRepository.save(ticket);

        String payload = qrSigningService.buildPayload(ticket);
        String signature = qrSigningService.sign(payload);
        ticket.setSignature(signature);
        ticket = ticketRepository.save(ticket);

        walletClient.debit(userId, product.getPrice());

        log.info("Ticket {} purchased by user {} for product {}", ticket.getId(), userId, productId);
        return ticket;
    }

    public Ticket getTicket(Long id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
    }

    public List<Ticket> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId);
    }

    public TicketDto toDto(Ticket ticket, String productName) {
        String qrJson = qrSigningService.buildQrJson(ticket, ticket.getSignature());
        return TicketDto.builder()
            .id(ticket.getId())
            .userId(ticket.getUserId())
            .productId(ticket.getProductId())
            .productName(productName)
            .nonce(ticket.getNonce())
            .validFrom(ticket.getValidFrom())
            .validUntil(ticket.getValidUntil())
            .signature(ticket.getSignature())
            .usageCount(ticket.getUsageCount())
            .maxUsage(ticket.getMaxUsage())
            .status(ticket.getStatus())
            .createdAt(ticket.getCreatedAt())
            .qrPayload(qrJson)
            .build();
    }
}
