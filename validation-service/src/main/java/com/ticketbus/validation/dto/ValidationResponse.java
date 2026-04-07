package com.ticketbus.validation.dto;

import com.ticketbus.common.domain.ValidationResult;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResponse {
    private ValidationResult result;
    private Long ticketId;
    private String message;
    /** Remaining usages for CARNET/PACK tickets (-1 for PASS = unlimited) */
    private int remainingUsages;
    /** Ticket expiry */
    private String validUntil;
    /** Product type: UNIT, PACK, PASS, CARNET */
    private String productType;
}
