package com.ticketbus.dto;

import com.ticketbus.entity.ValidationStatus;

public record ValidationResponse(
        boolean valid,
        ValidationStatus status,
        String message,
        TicketResponse ticketDetails
) {}
