package com.canteen.foodordering.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.canteen.foodordering.databinding.ActivityRegisterBinding;
import com.canteen.foodordering.student.StudentMainActivity;
import com.canteen.foodordering.utils.Constants;
import com.canteen.foodordering.viewmodels.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;
    private boolean isNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();

        binding.btnRegister.setOnClickListener(v -> handleRegistration());
        binding.tvLoginLink.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        authViewModel.getLoadingLiveData().observe(this, isLoading -> {
            if (isFinishing()) return;
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            if (isFinishing()) return;
            if (error != null && !error.isEmpty()) {
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getUserProfileLiveData().observe(this, user -> {
            if (isNavigated || isFinishing()) return;
            if (user != null) {
                isNavigated = true;
                Intent intent = new Intent(RegisterActivity.this, StudentMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void handleRegistration() {
        String name = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etFullName.setError("Full name is required");
            return;
        }
        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            return;
        }
        if (phone.isEmpty()) {
            binding.etPhone.setError("Phone number is required");
            return;
        }
        if (password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Every self-registered account is automatically assigned role = "student"
        authViewModel.register(name, email, phone, password, Constants.ROLE_STUDENT);
    }
}
