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
        // Listen to Firebase pendingCart so the cart bar updates when items are added
        cartViewModel.listenToPendingCart();
        cartViewModel.getPendingCartLiveData().observe(this, items -> {
            if (isFinishing()) return;
            if (items != null && !items.isEmpty()) {
                int count = 0;
                double total = 0;
                for (com.canteen.foodordering.models.CartItem item : items) {
                    count += item.getQuantity();
                    total += item.getTotalPrice();
                }
                binding.cardCartBar.setVisibility(View.VISIBLE);
                binding.tvCartBadgeItems.setText(count + (count == 1 ? " Item" : " Items"));
                binding.tvCartBadgeTotal.setText(String.format("\u20B9%.2f", total));
            } else {
                binding.cardCartBar.setVisibility(View.GONE);
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
