package com.ticketbus.common.domain;

public enum FraudAlertType {
    TEMPORAL_COLLISION,
    BLACKLISTED,
    INVALID_SIGNATURE,
    DOUBLE_SCAN,
    EXPIRED_USAGE,
    SUSPICIOUS_PATTERN
}

