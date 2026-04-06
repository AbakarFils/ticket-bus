package com.ticketbus.common.dto;

import com.ticketbus.common.domain.FraudAlertType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlertDto {
    private Long id;
    private Long ticketId;
    private FraudAlertType alertType;
    private String description;
    private String terminalId;
    private String location;
    private boolean resolved;
    private LocalDateTime createdAt;
}

