package com.ticketbus.dto;

import java.time.LocalDateTime;

public record ValidationEventDTO(
        String ticketNumber,
        String deviceId,
        String location,
        Double latitude,
        Double longitude,
        LocalDateTime validationTime,
        String result
) {}
