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

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketingService {

    private static final String PAYMENT_METHOD_WALLET = "WALLET";

    private final TicketRepository ticketRepository;
    private final ProductRepository productRepository;
    private final QrSigningService qrSigningService;
    private final WalletClient walletClient;
    private final MetricsService metricsService;

    @Value("${ticketing.ticket.validity-minutes:60}")
    private int validityMinutes;

    @Transactional
    public Ticket purchaseTicket(Long userId, Long productId, String paymentMethod) throws Exception {
        var timer = metricsService.startTicketPurchaseTimer();

        try {
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            if (!product.isActive()) {
                metricsService.recordTicketPurchaseFailed(product.getType().name(), paymentMethod, "PRODUCT_INACTIVE");
                throw new IllegalArgumentException("Product is not active: " + productId);
            }

            // Check wallet balance only for wallet payments
            if (PAYMENT_METHOD_WALLET.equals(paymentMethod)) {
                BigDecimal balance = walletClient.getBalance(userId);
                if (balance.compareTo(product.getPrice()) < 0) {
                    metricsService.recordInsufficientWalletBalance(userId);
                    metricsService.recordTicketPurchaseFailed(product.getType().name(), paymentMethod, "INSUFFICIENT_BALANCE");
                    throw new IllegalStateException("Insufficient wallet balance. Required: " + product.getPrice() + ", Available: " + balance);
                }
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime validUntil;
            if (product.getDurationDays() != null) {
                validUntil = now.plusDays(product.getDurationDays());
            } else {
                validUntil = now.plusMinutes(validityMinutes);
            }

            // PASS = unlimited usages (convention: maxUsage=999)
            // CARNET = fixed number of trips from product.maxUsage
            // UNIT/PACK = standard usage
            int maxUsage = product.getMaxUsage();
            if (product.getType() == ProductType.PASS) {
                maxUsage = 999; // unlimited rides for passes
            }

            Ticket ticket = Ticket.builder()
                .userId(userId)
                .productId(productId)
                .nonce(UUID.randomUUID().toString())
                .secret(UUID.randomUUID().toString()) // Secret for dynamic QR rotation
                .validFrom(now)
                .validUntil(validUntil)
                .maxUsage(maxUsage)
                .operatorId(product.getOperatorId())
                .zone(product.getZoneCode())
                .status(TicketStatus.ACTIVE)
                .build();

            ticket = ticketRepository.save(ticket);

            var qrTimer = metricsService.startQrGenerationTimer();
            String payload = qrSigningService.buildPayload(ticket);
            String signature = qrSigningService.sign(payload);
            ticket.setSignature(signature);
            ticket = ticketRepository.save(ticket);
            metricsService.recordQrGenerationDuration(qrTimer, "STATIC");
            metricsService.recordQrGenerated("STATIC");

            // Debit wallet only for wallet payments
            if (PAYMENT_METHOD_WALLET.equals(paymentMethod)) {
                walletClient.debit(userId, product.getPrice());
            }

            // Record success metrics
            metricsService.recordTicketPurchased(product.getType().name(), paymentMethod, product.getPrice());
            metricsService.recordTicketPurchaseDuration(timer, product.getType().name(), paymentMethod);

            log.info("Ticket {} purchased by user {} for product {} (type={}, payment={})",
                ticket.getId(), userId, productId, product.getType(), paymentMethod);
            return ticket;

        } catch (Exception e) {
            // Record failure metrics if product exists
            try {
                Product product = productRepository.findById(productId).orElse(null);
                String productType = product != null ? product.getType().name() : "UNKNOWN";
                metricsService.recordTicketPurchaseFailed(productType, paymentMethod, e.getClass().getSimpleName());
            } catch (Exception ignored) {
                // Ignore metric recording failures
            }
            throw e;
        }
    }

    public Ticket getTicket(Long id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
    }

    public List<Ticket> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId);
    }

    @Transactional
    public Ticket revokeTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
        if (ticket.getStatus() == TicketStatus.REVOKED) {
            throw new IllegalStateException("Ticket is already revoked");
        }
        ticket.setStatus(TicketStatus.REVOKED);
        log.info("Ticket {} revoked", id);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket cancelTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE tickets can be cancelled. Status: " + ticket.getStatus());
        }
        ticket.setStatus(TicketStatus.REVOKED);
        log.info("Ticket {} cancelled", id);
        return ticketRepository.save(ticket);
    }

    public TicketDto toDto(Ticket ticket, String productName) {
        Product product = productRepository.findById(ticket.getProductId()).orElse(null);
        String productType = product != null ? product.getType().name() : "";
        String qrJson = qrSigningService.buildQrJson(ticket, ticket.getSignature(), productType);
        return TicketDto.builder()
            .id(ticket.getId())
            .userId(ticket.getUserId())
            .productId(ticket.getProductId())
            .productName(productName)
            .productType(productType)
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
