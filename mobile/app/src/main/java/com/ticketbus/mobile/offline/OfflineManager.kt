package com.ticketbus.mobile.offline

import android.content.Context
import androidx.work.*
import com.ticketbus.mobile.data.local.AppDatabase
import com.ticketbus.mobile.data.local.entity.PendingValidation
import com.ticketbus.mobile.util.Constants
import com.ticketbus.mobile.util.NetworkMonitor
import com.ticketbus.mobile.util.PreferenceManager
import java.util.concurrent.TimeUnit

class OfflineManager(
    private val context: Context,
    private val database: AppDatabase,
    private val prefs: PreferenceManager,
    private val networkMonitor: NetworkMonitor
) {
    fun isOnline() = networkMonitor.isConnected()
    suspend fun getPendingCount() = database.validationEventDao().getUnsyncedCount()
    suspend fun getBlacklistCount() = database.blacklistDao().count()

    fun schedulePeriodicSync() {
        val req = PeriodicWorkRequestBuilder<SyncWorker>(Constants.SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(Constants.SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, req)
    }

    fun triggerImmediateSync() {
        val req = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueue(req)
    }

    suspend fun isBlacklisted(ticketNumber: String) =
        database.blacklistDao().findByTicketNumber(ticketNumber) != null

    suspend fun hasSeenNonce(ticketNumber: String) =
        database.ticketCacheDao().findByTicketNumber(ticketNumber) != null

    suspend fun recordValidation(ticketNumber: String, nonce: String, deviceId: String, location: String, result: String) {
        database.ticketCacheDao().insert(com.ticketbus.mobile.data.local.entity.CachedTicket(ticketNumber, nonce))
        database.validationEventDao().insert(
            PendingValidation(qrData = "", deviceId = deviceId, location = location, latitude = null, longitude = null, result = result)
        )
    }
}
