package com.ticketbus.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketRequest(
        String passengerName,
        String passengerEmail,
        String routeName,
        String departureLocation,
        String arrivalLocation,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal price,
        LocalDateTime activationWindowStart,
        LocalDateTime activationWindowEnd,
        int maxUsageCount
) {}
