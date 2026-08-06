package com.canteen.foodordering.student;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ActivityStudentMainBinding;

public class StudentMainActivity extends AppCompatActivity {

    private ActivityStudentMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Default fragment
        loadFragment(new StudentHomeFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new StudentHomeFragment();
            } else if (itemId == R.id.nav_orders) {
                selectedFragment = new StudentOrdersFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new StudentProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
