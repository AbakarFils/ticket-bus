package com.ticketbus.mobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ticketbus.mobile.data.remote.RetrofitClient
import com.ticketbus.mobile.data.remote.dto.LoginRequest
import com.ticketbus.mobile.databinding.ActivityLoginBinding
import com.ticketbus.mobile.util.PreferenceManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager(this)
        if (prefs.isLoggedIn()) { startScanner(); return }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.btnLogin.isEnabled = false
            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.create().login(LoginRequest(username, password))
                    if (resp.isSuccessful && resp.body() != null) {
                        prefs.authToken = resp.body()!!.token
                        prefs.username = resp.body()!!.username
                        startScanner()
                    } else {
                        Toast.makeText(this@LoginActivity, "Identifiants invalides", Toast.LENGTH_SHORT).show()
                        binding.btnLogin.isEnabled = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun startScanner() { startActivity(Intent(this, ScannerActivity::class.java)); finish() }
}
