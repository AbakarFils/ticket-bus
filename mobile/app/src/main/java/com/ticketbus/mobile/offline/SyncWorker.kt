package com.ticketbus.mobile.offline

import android.content.Context
import androidx.work.*
import com.ticketbus.mobile.data.local.AppDatabase
import com.ticketbus.mobile.data.local.entity.BlacklistEntryLocal
import com.ticketbus.mobile.data.remote.RetrofitClient
import com.ticketbus.mobile.data.remote.dto.*
import com.ticketbus.mobile.util.PreferenceManager
import java.time.Instant

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val prefs = PreferenceManager(applicationContext)
        val db = AppDatabase.getInstance(applicationContext)
        val api = RetrofitClient.create(prefs.authToken)

        // Upload pending validations
        val pending = db.validationEventDao().getUnsynced()
        if (pending.isNotEmpty()) {
            val dtos = pending.map {
                PendingValidationDto(
                    ticketNumber = "", deviceId = it.deviceId, location = it.location,
                    latitude = it.latitude, longitude = it.longitude,
                    validationTime = Instant.ofEpochMilli(it.validationTime).toString(),
                    result = it.result
                )
            }
            val req = SyncUploadRequest(prefs.username ?: "unknown", dtos, prefs.lastSync)
            try {
                val resp = api.uploadSync(req)
                if (resp.isSuccessful) pending.forEach { db.validationEventDao().markSynced(it.id) }
            } catch (_: Exception) {}
        }

        // Fetch blacklist updates
        try {
            val resp = api.getBlacklist(prefs.lastSync)
            if (resp.isSuccessful) {
                val entries = resp.body()?.map { BlacklistEntryLocal(it.ticketNumber, it.reason) } ?: emptyList()
                if (entries.isNotEmpty()) db.blacklistDao().insertAll(entries)
            }
        } catch (_: Exception) {}

        // Fetch public key
        try {
            val resp = api.getPublicKey()
            if (resp.isSuccessful) prefs.publicKey = resp.body()?.key
        } catch (_: Exception) {}

        prefs.lastSync = Instant.now().toString()
        return Result.success()
    }
}
