package com.ticketbus.mobile.offline;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ticketbus.mobile.data.local.AppDatabase;
import com.ticketbus.mobile.data.local.entity.BlacklistEntryLocal;
import com.ticketbus.mobile.data.local.entity.PendingValidation;
import com.ticketbus.mobile.data.remote.ApiService;
import com.ticketbus.mobile.data.remote.RetrofitClient;
import com.ticketbus.mobile.data.remote.dto.BlacklistDto;
import com.ticketbus.mobile.data.remote.dto.PendingValidationDto;
import com.ticketbus.mobile.data.remote.dto.PublicKeyDto;
import com.ticketbus.mobile.data.remote.dto.SyncResponse;
import com.ticketbus.mobile.data.remote.dto.SyncUploadRequest;
import com.ticketbus.mobile.util.PreferenceManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        PreferenceManager prefs = new PreferenceManager(ctx);
        AppDatabase db = AppDatabase.getInstance(ctx);
        ApiService api = RetrofitClient.create(prefs.getAuthToken());

        // Upload pending validations
        List<PendingValidation> pending = db.validationEventDao().getUnsynced();
        if (!pending.isEmpty()) {
            List<PendingValidationDto> dtos = new ArrayList<>();
            for (PendingValidation pv : pending) {
                dtos.add(new PendingValidationDto(
                    "", pv.deviceId, pv.location, pv.latitude, pv.longitude,
                    Instant.ofEpochMilli(pv.validationTime).toString(), pv.result
                ));
            }
            SyncUploadRequest req = new SyncUploadRequest(
                prefs.getUsername() != null ? prefs.getUsername() : "unknown",
                dtos,
                prefs.getLastSync()
            );
            try {
                Response<SyncResponse> resp = api.uploadSync(req).execute();
                if (resp.isSuccessful()) {
                    for (PendingValidation pv : pending) {
                        db.validationEventDao().markSynced(pv.id);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Upload failed", e);
            }
        }

        // Fetch blacklist updates
        try {
            Response<List<BlacklistDto>> resp = api.getBlacklist(prefs.getLastSync()).execute();
            if (resp.isSuccessful() && resp.body() != null) {
                List<BlacklistEntryLocal> entries = new ArrayList<>();
                for (BlacklistDto dto : resp.body()) {
                    entries.add(new BlacklistEntryLocal(dto.ticketNumber, dto.reason));
                }
                if (!entries.isEmpty()) {
                    db.blacklistDao().insertAll(entries);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Blacklist fetch failed", e);
        }

        // Fetch public key
        try {
            Response<PublicKeyDto> resp = api.getPublicKey().execute();
            if (resp.isSuccessful() && resp.body() != null) {
                prefs.setPublicKey(resp.body().key);
            }
        } catch (Exception e) {
            Log.w(TAG, "Public key fetch failed", e);
        }

        prefs.setLastSync(Instant.now().toString());
        return Result.success();
    }
}
