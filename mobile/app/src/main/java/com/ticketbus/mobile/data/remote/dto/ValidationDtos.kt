package com.ticketbus.mobile.data.remote.dto

data class ValidationRequest(
    val qrCodeData: String,
    val deviceId: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val validationTime: String
)
data class ValidationResponse(val valid: Boolean, val status: String, val message: String)
