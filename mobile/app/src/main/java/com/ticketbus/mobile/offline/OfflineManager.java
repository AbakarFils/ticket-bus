package com.ticketbus.mobile.offline;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ticketbus.mobile.data.local.AppDatabase;
import com.ticketbus.mobile.data.local.entity.CachedTicket;
import com.ticketbus.mobile.data.local.entity.PendingValidation;
import com.ticketbus.mobile.util.Constants;
import com.ticketbus.mobile.util.NetworkMonitor;
import com.ticketbus.mobile.util.PreferenceManager;

import java.util.concurrent.TimeUnit;

public class OfflineManager {
    private final Context context;
    private final AppDatabase database;
    private final PreferenceManager prefs;
    private final NetworkMonitor networkMonitor;

    public OfflineManager(Context context, AppDatabase database,
                          PreferenceManager prefs, NetworkMonitor networkMonitor) {
        this.context = context;
        this.database = database;
        this.prefs = prefs;
        this.networkMonitor = networkMonitor;
    }

    public boolean isOnline() {
        return networkMonitor.isConnected();
    }

    public int getPendingCount() {
        return database.validationEventDao().getUnsyncedCount();
    }

    public int getBlacklistCount() {
        return database.blacklistDao().count();
    }

    public void schedulePeriodicSync() {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
            SyncWorker.class, Constants.SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build();
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                Constants.SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                req
            );
    }

    public void triggerImmediateSync() {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(SyncWorker.class)
            .setConstraints(constraints)
            .build();
        WorkManager.getInstance(context).enqueue(req);
    }

    public boolean isBlacklisted(String ticketNumber) {
        return database.blacklistDao().findByTicketNumber(ticketNumber) != null;
    }

    public boolean hasSeenNonce(String ticketNumber) {
        return database.ticketCacheDao().findByTicketNumber(ticketNumber) != null;
    }

    public void recordValidation(String ticketNumber, String nonce,
                                  String deviceId, String location, String result) {
        database.ticketCacheDao().insert(new CachedTicket(ticketNumber, nonce));
        database.validationEventDao().insert(
            new PendingValidation("", deviceId, location, null, null, result)
        );
    }
}
