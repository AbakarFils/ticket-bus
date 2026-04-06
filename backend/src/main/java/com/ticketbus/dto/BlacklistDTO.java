package com.ticketbus.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BlacklistDTO(
        UUID id,
        String ticketNumber,
        String reason,
        LocalDateTime blacklistedAt,
        boolean active
) {}
