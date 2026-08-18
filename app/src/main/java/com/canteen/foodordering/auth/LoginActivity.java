package com.canteen.foodordering.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.canteen.foodordering.admin.AdminMainActivity;
import com.canteen.foodordering.databinding.ActivityLoginBinding;
import com.canteen.foodordering.student.StudentMainActivity;
import com.canteen.foodordering.utils.Constants;
import com.canteen.foodordering.viewmodels.AuthViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private boolean isNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();

        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void observeViewModel() {
        authViewModel.getLoadingLiveData().observe(this, isLoading -> {
            if (isFinishing()) return;
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            if (isFinishing()) return;
            if (error != null && !error.isEmpty()) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getUserProfileLiveData().observe(this, user -> {
            if (isNavigated || isFinishing()) return;
            if (user != null) {
                isNavigated = true;
                Intent intent;
                if (Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
                    intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                } else if (Constants.ROLE_STUDENT.equalsIgnoreCase(user.getRole())) {
                    intent = new Intent(LoginActivity.this, StudentMainActivity.class);
                } else {
                    authViewModel.logout();
                    Toast.makeText(LoginActivity.this, "Access Denied", Toast.LENGTH_LONG).show();
                    isNavigated = false;
                    return;
                }
                startActivity(intent);
                finish();
            }
        });
    }

    private void handleLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required");
            return;
        }

        authViewModel.login(email, password);
    }
}
