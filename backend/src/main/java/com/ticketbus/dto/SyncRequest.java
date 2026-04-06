package com.ticketbus.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SyncRequest(
        String deviceId,
        LocalDateTime lastSyncTime,
        List<ValidationEventDTO> validationEvents
) {}
