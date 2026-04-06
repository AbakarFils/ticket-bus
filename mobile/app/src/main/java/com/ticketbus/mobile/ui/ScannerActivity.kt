package com.ticketbus.mobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.zxing.integration.android.IntentIntegrator
import com.ticketbus.mobile.crypto.SignatureVerifier
import com.ticketbus.mobile.data.local.AppDatabase
import com.ticketbus.mobile.data.remote.RetrofitClient
import com.ticketbus.mobile.data.remote.dto.ValidationRequest
import com.ticketbus.mobile.databinding.ActivityScannerBinding
import com.ticketbus.mobile.offline.OfflineManager
import com.ticketbus.mobile.util.NetworkMonitor
import com.ticketbus.mobile.util.PreferenceManager
import kotlinx.coroutines.launch
import java.time.Instant

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding
    private lateinit var prefs: PreferenceManager
    private lateinit var offlineManager: OfflineManager
    private lateinit var signatureVerifier: SignatureVerifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferenceManager(this)
        val db = AppDatabase.getInstance(this)
        offlineManager = OfflineManager(this, db, prefs, NetworkMonitor(this))
        signatureVerifier = SignatureVerifier(prefs)
        offlineManager.schedulePeriodicSync()

        updateStatus()
        binding.btnScan.setOnClickListener {
            IntentIntegrator(this).apply {
                setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                setPrompt("Scannez le QR code du ticket")
                setCameraId(0)
                setBeepEnabled(true)
                initiateScan()
            }
        }
        binding.btnSync.setOnClickListener {
            offlineManager.triggerImmediateSync()
            Toast.makeText(this, "Synchronisation en cours...", Toast.LENGTH_SHORT).show()
        }
        binding.btnSyncStatus.setOnClickListener {
            startActivity(Intent(this, SyncStatusActivity::class.java))
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            processQrCode(result.contents)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun processQrCode(qrJson: String) {
        lifecycleScope.launch {
            val isOnline = offlineManager.isOnline()
            if (isOnline) {
                try {
                    val api = RetrofitClient.create(prefs.authToken)
                    val resp = api.validate(
                        ValidationRequest(qrJson, prefs.username ?: "device", "Bus", null, null, Instant.now().toString())
                    )
                    if (resp.isSuccessful) {
                        val r = resp.body()!!
                        showResult(r.valid, r.status, r.message)
                    } else {
                        showResult(false, "INVALID", "Erreur serveur")
                    }
                } catch (e: Exception) {
                    offlineValidate(qrJson)
                }
            } else {
                offlineValidate(qrJson)
            }
        }
    }

    private suspend fun offlineValidate(qrJson: String) {
        val (payload, sigValid) = signatureVerifier.parseAndVerify(qrJson)
        if (payload == null || !sigValid) { showResult(false, "INVALID", "Signature invalide"); return }
        if (signatureVerifier.isExpired(payload.validUntil)) { showResult(false, "EXPIRED", "Ticket expiré"); return }
        if (offlineManager.isBlacklisted(payload.ticketNumber)) { showResult(false, "BLACKLISTED", "Ticket blacklisté"); return }
        if (offlineManager.hasSeenNonce(payload.ticketNumber)) { showResult(false, "SUSPECT", "Ticket déjà utilisé"); return }
        offlineManager.recordValidation(payload.ticketNumber, payload.nonce, prefs.username ?: "device", "Offline", "VALID")
        showResult(true, "VALID", "Ticket valide (hors ligne)")
    }

    private fun showResult(valid: Boolean, status: String, message: String) {
        val title = when {
            valid -> "✅ VALIDE"
            status == "SUSPECT" -> "⚠️ SUSPECT"
            else -> "❌ INVALIDE"
        }
        val icon = when {
            valid -> "✅"
            status == "SUSPECT" -> "⚠️"
            else -> "❌"
        }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("$icon $message\nStatut: $status")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateStatus() {
        val online = offlineManager.isOnline()
        binding.tvStatus.text = if (online) "🟢 En ligne" else "🔴 Hors ligne"
    }

    override fun onResume() { super.onResume(); updateStatus() }
}
