package com.ticketbus.dto;

import java.time.LocalDateTime;

public record PublicKeyDTO(
        String key,
        String algorithm,
        LocalDateTime issuedAt
) {}
