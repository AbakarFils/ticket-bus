package com.ticketbus.audit.dto;

public record CreateFraudAlertRequest(Long ticketId, Long userId, String terminalId,
                                      String alertType, String description) {}
