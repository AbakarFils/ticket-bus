package com.ticketbus.customer.dto;

import java.time.LocalDateTime;

public record CustomerDto(Long id, String email, String firstName, String lastName,
                          String phone, String role, boolean active, LocalDateTime createdAt) {}
