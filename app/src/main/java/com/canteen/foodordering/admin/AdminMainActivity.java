package com.canteen.foodordering.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.canteen.foodordering.R;
import com.canteen.foodordering.auth.LoginActivity;
import com.canteen.foodordering.databinding.ActivityAdminMainBinding;
import com.canteen.foodordering.utils.Constants;
import com.canteen.foodordering.viewmodels.AuthViewModel;

public class AdminMainActivity extends AppCompatActivity {
    private ActivityAdminMainBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        verifyAdminProtection();

        setupNavigation();

        if (savedInstanceState == null) {
            loadFragment(new AdminDashboardFragment());
        }
    }

    private void verifyAdminProtection() {
        if (authViewModel.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        authViewModel.getUserProfileLiveData().observe(this, user -> {
            if (user == null || !Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
                Toast.makeText(AdminMainActivity.this, "Access Denied: Admin privileges required", Toast.LENGTH_LONG).show();
                authViewModel.logout();
                redirectToLogin();
            }
        });

        authViewModel.fetchUserProfile(authViewModel.getCurrentUser().getUid());
    }

    private void redirectToLogin() {
        Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigation() {
        binding.adminBottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                fragment = new AdminDashboardFragment();
            } else if (itemId == R.id.nav_manage_menu) {
                fragment = new AdminFoodListFragment();
            } else if (itemId == R.id.nav_admin_orders) {
                fragment = new AdminOrdersFragment();
            } else if (itemId == R.id.nav_users) {
                fragment = new AdminUsersFragment();
            }
            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null && !isFinishing()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.adminFragmentContainer, fragment)
                    .commitAllowingStateLoss();
            return true;
        }
        return false;
    }
}
