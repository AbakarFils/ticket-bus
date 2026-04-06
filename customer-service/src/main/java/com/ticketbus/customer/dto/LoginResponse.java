package com.ticketbus.customer.dto;

public record LoginResponse(String token, Long userId, String email, String role) {}
