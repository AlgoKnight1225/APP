package com.canteen.foodordering.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.canteen.foodordering.databinding.ActivityForgotPasswordBinding;
import com.canteen.foodordering.viewmodels.AuthViewModel;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();

        binding.btnSendReset.setOnClickListener(v -> handleReset());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        authViewModel.getLoadingLiveData().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSendReset.setEnabled(!isLoading);
        });

        authViewModel.getErrorLiveData().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                if ("RESET_SUCCESS".equals(message)) {
                    Toast.makeText(this, "Password reset link sent to your email!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void handleReset() {
        String email = binding.etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            binding.etEmail.setError("Email address is required");
            return;
        }
        authViewModel.sendPasswordResetEmail(email);
    }
}
