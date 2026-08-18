package com.canteen.foodordering.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.canteen.foodordering.admin.AdminMainActivity;
import com.canteen.foodordering.databinding.ActivitySplashBinding;
import com.canteen.foodordering.student.StudentMainActivity;
import com.canteen.foodordering.utils.Constants;
import com.canteen.foodordering.viewmodels.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private AuthViewModel authViewModel;
    private boolean isNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Logo animations
        binding.ivSplashLogo.setAlpha(0f);
        binding.ivSplashLogo.setScaleX(0.7f);
        binding.ivSplashLogo.setScaleY(0.7f);
        binding.ivSplashLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .start();

        binding.tvTagline.setAlpha(0f);
        binding.tvTagline.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(300)
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserSession, 2000);
    }

    private void checkUserSession() {
        if (isNavigated || isFinishing()) return;

        try {
            FirebaseUser currentUser = authViewModel.getCurrentUser();
            if (currentUser != null) {
                authViewModel.getUserProfileLiveData().observe(this, user -> {
                    if (isNavigated || isFinishing()) return;
                    if (user != null) {
                        isNavigated = true;
                        Intent intent;
                        if (Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
                            intent = new Intent(SplashActivity.this, AdminMainActivity.class);
                        } else {
                            intent = new Intent(SplashActivity.this, StudentMainActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    }
                });

                authViewModel.getErrorLiveData().observe(this, error -> {
                    if (isNavigated || isFinishing()) return;
                    if (error != null && !error.isEmpty()) {
                        isNavigated = true;
                        navigateToLogin();
                    }
                });

                authViewModel.fetchUserProfile(currentUser.getUid());
            } else {
                isNavigated = true;
                navigateToLogin();
            }
        } catch (Exception e) {
            isNavigated = true;
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        if (isFinishing()) return;
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
