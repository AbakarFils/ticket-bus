package com.ticketbus.dto;

import com.ticketbus.entity.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String ticketNumber,
        String passengerName,
        String passengerEmail,
        String routeName,
        String departureLocation,
        String arrivalLocation,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal price,
        TicketStatus status,
        UUID nonce,
        int usageCount,
        int maxUsageCount,
        LocalDateTime activationWindowStart,
        LocalDateTime activationWindowEnd,
        String qrCodeData,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
