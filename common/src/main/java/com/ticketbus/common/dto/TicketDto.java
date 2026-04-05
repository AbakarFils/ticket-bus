package com.ticketbus.common.dto;

import com.ticketbus.common.domain.TicketStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDto {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String nonce;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String signature;
    private int usageCount;
    private int maxUsage;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private String qrPayload;
}
