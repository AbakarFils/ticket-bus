package com.ticketbus.mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ticketbus.mobile.R;
import com.ticketbus.mobile.crypto.SignatureVerifier;
import com.ticketbus.mobile.data.local.AppDatabase;
import com.ticketbus.mobile.data.remote.RetrofitClient;
import com.ticketbus.mobile.data.remote.dto.ValidationRequest;
import com.ticketbus.mobile.data.remote.dto.ValidationResponse;
import com.ticketbus.mobile.databinding.ActivityScannerBinding;
import com.ticketbus.mobile.offline.OfflineManager;
import com.ticketbus.mobile.util.NetworkMonitor;
import com.ticketbus.mobile.util.PreferenceManager;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScannerActivity extends AppCompatActivity {
    private ActivityScannerBinding binding;
    private PreferenceManager prefs;
    private OfflineManager offlineManager;
    private SignatureVerifier signatureVerifier;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new PreferenceManager(this);
        AppDatabase db = AppDatabase.getInstance(this);
        offlineManager = new OfflineManager(this, db, prefs, new NetworkMonitor(this));
        signatureVerifier = new SignatureVerifier(prefs);

        offlineManager.schedulePeriodicSync();
        updateStatus();

        binding.btnScan.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scannez le QR code du ticket");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.initiateScan();
        });

        binding.btnSync.setOnClickListener(v -> {
            offlineManager.triggerImmediateSync();
            Toast.makeText(this, "Synchronisation en cours...", Toast.LENGTH_SHORT).show();
        });

        binding.btnSyncStatus.setOnClickListener(v ->
            startActivity(new Intent(this, SyncStatusActivity.class))
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            processQrCode(result.getContents());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processQrCode(String qrJson) {
        if (offlineManager.isOnline()) {
            RetrofitClient.create(prefs.getAuthToken())
                .validate(new ValidationRequest(
                    qrJson,
                    prefs.getUsername() != null ? prefs.getUsername() : "device",
                    "Bus", null, null,
                    Instant.now().toString()
                ))
                .enqueue(new Callback<ValidationResponse>() {
                    @Override
                    public void onResponse(Call<ValidationResponse> call, Response<ValidationResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ValidationResponse r = response.body();
                            showResult(r.valid, r.status, r.message);
                        } else {
                            showResult(false, "INVALID", "Erreur serveur");
                        }
                    }
                    @Override
                    public void onFailure(Call<ValidationResponse> call, Throwable t) {
                        offlineValidate(qrJson);
                    }
                });
        } else {
            offlineValidate(qrJson);
        }
    }

    private void offlineValidate(String qrJson) {
        executor.execute(() -> {
            SignatureVerifier.ParseResult pr = signatureVerifier.parseAndVerify(qrJson);
            if (pr.payload == null || !pr.signatureValid) {
                showResult(false, "INVALID", "Signature invalide");
                return;
            }
            if (signatureVerifier.isExpired(pr.payload.validUntil)) {
                showResult(false, "EXPIRED", "Ticket expiré");
                return;
            }
            if (offlineManager.isBlacklisted(pr.payload.ticketNumber)) {
                showResult(false, "BLACKLISTED", "Ticket blacklisté");
                return;
            }
            if (offlineManager.hasSeenNonce(pr.payload.ticketNumber)) {
                showResult(false, "SUSPECT", "Ticket déjà utilisé");
                return;
            }
            offlineManager.recordValidation(
                pr.payload.ticketNumber, pr.payload.nonce,
                prefs.getUsername() != null ? prefs.getUsername() : "device",
                "Offline", "VALID"
            );
            showResult(true, "VALID", "Ticket valide (hors ligne)");
        });
    }

    private void showResult(boolean valid, String status, String message) {
        String title;
        if (valid) title = "✅ VALIDE";
        else if ("SUSPECT".equals(status)) title = "⚠️ SUSPECT";
        else title = "❌ INVALIDE";

        runOnUiThread(() ->
            new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message + "\nStatut: " + status)
                .setPositiveButton("OK", null)
                .show()
        );
    }

    private void updateStatus() {
        binding.tvStatus.setText(offlineManager.isOnline() ? "🟢 En ligne" : "🔴 Hors ligne");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
