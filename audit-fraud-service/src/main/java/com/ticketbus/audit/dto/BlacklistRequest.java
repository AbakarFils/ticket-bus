package com.ticketbus.audit.dto;

public record BlacklistRequest(Long ticketId, String reason, String blacklistedBy) {}
