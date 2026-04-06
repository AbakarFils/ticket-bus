package com.ticketbus.dto;

public record AuthRequest(
        String username,
        String password
) {}
