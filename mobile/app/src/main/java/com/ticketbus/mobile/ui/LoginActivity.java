package com.ticketbus.mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ticketbus.mobile.data.remote.RetrofitClient;
import com.ticketbus.mobile.data.remote.dto.AuthResponse;
import com.ticketbus.mobile.data.remote.dto.LoginRequest;
import com.ticketbus.mobile.databinding.ActivityLoginBinding;
import com.ticketbus.mobile.util.PreferenceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private PreferenceManager prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new PreferenceManager(this);
        if (prefs.isLoggedIn()) {
            startScanner();
            return;
        }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.btnLogin.setEnabled(false);
            RetrofitClient.create(null).login(new LoginRequest(username, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            prefs.setAuthToken(response.body().token);
                            prefs.setUsername(response.body().username);
                            startScanner();
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "Identifiants invalides", Toast.LENGTH_SHORT).show();
                                binding.btnLogin.setEnabled(true);
                            });
                        }
                    }
                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            binding.btnLogin.setEnabled(true);
                        });
                    }
                });
        });
    }

    private void startScanner() {
        startActivity(new Intent(this, ScannerActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
