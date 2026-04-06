package com.ticketbus.dto;

import java.time.LocalDateTime;

public record ValidationRequest(
        String qrCodeData,
        String deviceId,
        String location,
        Double latitude,
        Double longitude,
        LocalDateTime validationTime
) {}
