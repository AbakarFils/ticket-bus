package com.ticketbus.mobile.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ticketbus.mobile.data.local.AppDatabase;
import com.ticketbus.mobile.databinding.ActivitySyncBinding;
import com.ticketbus.mobile.offline.OfflineManager;
import com.ticketbus.mobile.util.NetworkMonitor;
import com.ticketbus.mobile.util.PreferenceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncStatusActivity extends AppCompatActivity {
    private ActivitySyncBinding binding;
    private OfflineManager offlineManager;
    private PreferenceManager prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySyncBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new PreferenceManager(this);
        AppDatabase db = AppDatabase.getInstance(this);
        offlineManager = new OfflineManager(this, db, prefs, new NetworkMonitor(this));

        loadStatus();

        binding.btnSyncNow.setOnClickListener(v -> {
            offlineManager.triggerImmediateSync();
            Toast.makeText(this, "Synchronisation lancée", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadStatus() {
        executor.execute(() -> {
            int pending = offlineManager.getPendingCount();
            int blacklistCount = offlineManager.getBlacklistCount();
            runOnUiThread(() -> {
                binding.tvNetworkStatus.setText(offlineManager.isOnline() ? "🟢 En ligne" : "🔴 Hors ligne");
                binding.tvPendingCount.setText("Validations en attente: " + pending);
                binding.tvLastSync.setText("Dernière sync: " + (prefs.getLastSync() != null ? prefs.getLastSync() : "Jamais"));
                binding.tvBlacklistCount.setText("Blacklist en cache: " + blacklistCount + " entrées");
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
