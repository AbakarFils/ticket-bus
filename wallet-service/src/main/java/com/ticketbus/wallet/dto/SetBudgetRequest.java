package com.ticketbus.wallet.dto;

import java.math.BigDecimal;

public record SetBudgetRequest(BigDecimal dailyLimit, BigDecimal weeklyLimit, BigDecimal monthlyLimit) {}
