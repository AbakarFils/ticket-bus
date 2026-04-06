package com.ticketbus.dto;

import com.ticketbus.entity.UserRole;

public record AuthResponse(
        String token,
        String username,
        UserRole role
) {}
