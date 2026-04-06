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
}
