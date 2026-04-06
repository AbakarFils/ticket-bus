package com.ticketbus.mobile.data.remote.dto;

import java.util.List;

public class SyncResponse {
    public List<BlacklistDto> blacklistUpdates;
    public PublicKeyDto publicKey;
    public int processedEvents;
}
