package com.ticketbus.dto;

import java.util.List;

public record SyncResponse(
        List<BlacklistDTO> blacklistUpdates,
        PublicKeyDTO publicKey,
        int processedEvents
) {}
