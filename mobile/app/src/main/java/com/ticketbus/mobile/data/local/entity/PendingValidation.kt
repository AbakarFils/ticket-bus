package com.ticketbus.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_validations")
data class PendingValidation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val qrData: String,
    val deviceId: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val validationTime: Long = System.currentTimeMillis(),
    val result: String,
    val synced: Boolean = false
)
