package com.ticketbus.mobile.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ticketbus.mobile.data.local.AppDatabase
import com.ticketbus.mobile.databinding.ActivitySyncBinding
import com.ticketbus.mobile.offline.OfflineManager
import com.ticketbus.mobile.util.NetworkMonitor
import com.ticketbus.mobile.util.PreferenceManager
import kotlinx.coroutines.launch

class SyncStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySyncBinding
    private lateinit var offlineManager: OfflineManager
    private lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferenceManager(this)
        val db = AppDatabase.getInstance(this)
        offlineManager = OfflineManager(this, db, prefs, NetworkMonitor(this))
        loadStatus()
        binding.btnSyncNow.setOnClickListener {
            offlineManager.triggerImmediateSync()
            Toast.makeText(this, "Synchronisation lancée", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadStatus() {
        lifecycleScope.launch {
            val pending = offlineManager.getPendingCount()
            val blacklistCount = offlineManager.getBlacklistCount()
            binding.tvNetworkStatus.text = if (offlineManager.isOnline()) "🟢 En ligne" else "🔴 Hors ligne"
            binding.tvPendingCount.text = "Validations en attente: $pending"
            binding.tvLastSync.text = "Dernière sync: ${prefs.lastSync ?: "Jamais"}"
            binding.tvBlacklistCount.text = "Blacklist en cache: $blacklistCount entrées"
        }
    }
}
