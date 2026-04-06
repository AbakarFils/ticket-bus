package com.ticketbus.mobile.data.remote.dto

data class SyncUploadRequest(
    val deviceId: String,
    val validationEvents: List<PendingValidationDto>,
    val lastSyncTime: String?
)
data class PendingValidationDto(
    val ticketNumber: String,
    val deviceId: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val validationTime: String,
    val result: String
)
data class BlacklistDto(val ticketNumber: String, val reason: String, val blacklistedAt: String)
data class PublicKeyDto(val key: String, val algorithm: String, val issuedAt: String)
data class SyncResponse(val blacklistUpdates: List<BlacklistDto>, val publicKey: PublicKeyDto?, val processedEvents: Int)
