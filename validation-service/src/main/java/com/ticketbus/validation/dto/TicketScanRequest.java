package com.ticketbus.validation.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketScanRequest {
    private String qrPayload;
    private String signature;
    private String terminalId;
    private String location;
    private boolean offline;
    /** ISO-8601 timestamp from agent device when validated offline */
    private String offlineTimestamp;
}
