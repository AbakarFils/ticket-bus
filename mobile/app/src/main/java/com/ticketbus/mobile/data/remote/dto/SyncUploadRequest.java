package com.ticketbus.mobile.data.remote.dto;

import java.util.List;

public class SyncUploadRequest {
    public String deviceId;
    public List<PendingValidationDto> validationEvents;
    public String lastSyncTime;

    public SyncUploadRequest(String deviceId, List<PendingValidationDto> validationEvents, String lastSyncTime) {
        this.deviceId = deviceId;
        this.validationEvents = validationEvents;
        this.lastSyncTime = lastSyncTime;
    }
}
