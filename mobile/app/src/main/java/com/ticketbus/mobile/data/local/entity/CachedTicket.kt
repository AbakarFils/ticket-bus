package com.ticketbus.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_tickets")
data class CachedTicket(
    @PrimaryKey val ticketNumber: String,
    val nonce: String,
    val validatedAt: Long = System.currentTimeMillis(),
    val validCount: Int = 1
)
