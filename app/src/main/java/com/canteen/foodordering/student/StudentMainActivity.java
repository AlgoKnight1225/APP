package com.canteen.foodordering.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ActivityStudentMainBinding;
import com.canteen.foodordering.viewmodels.CartViewModel;

public class StudentMainActivity extends AppCompatActivity {
    private ActivityStudentMainBinding binding;
    private CartViewModel cartViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        observeCart();
        setupNavigation();

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new StudentHomeFragment());
        }

        binding.cardCartBar.setOnClickListener(v -> {
            startActivity(new Intent(StudentMainActivity.this, CartActivity.class));
        });
    }

    private void observeCart() {
        cartViewModel.getItemCountLiveData().observe(this, count -> {
            if (isFinishing()) return;
            if (count != null && count > 0) {
                binding.cardCartBar.setVisibility(View.VISIBLE);
                binding.tvCartBadgeItems.setText(count + (count == 1 ? " Item" : " Items"));
            } else {
                binding.cardCartBar.setVisibility(View.GONE);
            }
        });

        cartViewModel.getTotalAmountLiveData().observe(this, total -> {
            if (isFinishing()) return;
            if (total != null) {
                binding.tvCartBadgeTotal.setText(String.format("₹%.2f", total));
            }
        });
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                fragment = new StudentHomeFragment();
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(StudentMainActivity.this, CartActivity.class));
                return false;
            } else if (itemId == R.id.nav_orders) {
                fragment = new StudentOrdersFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new StudentProfileFragment();
            }
            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null && !isFinishing()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commitAllowingStateLoss();
            return true;
        }
        return false;
    }
}
