package com.ticketbus.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blacklist")
data class BlacklistEntryLocal(
    @PrimaryKey val ticketNumber: String,
    val reason: String,
    val blacklistedAt: Long = System.currentTimeMillis()
)
