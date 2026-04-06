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
}
