package com.canteen.foodordering.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.canteen.foodordering.R;
import com.canteen.foodordering.auth.LoginActivity;
import com.canteen.foodordering.databinding.ActivityAddEditFoodBinding;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.utils.Constants;
import com.canteen.foodordering.viewmodels.AuthViewModel;
import com.canteen.foodordering.viewmodels.FoodViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddEditFoodActivity extends AppCompatActivity {
    private ActivityAddEditFoodBinding binding;
    private FoodViewModel foodViewModel;
    private AuthViewModel authViewModel;
    private FoodItem editingFoodItem;
    private List<String> categoriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        verifyAdminProtection();
        setupImageUrlWatcher();
        setupCategorySpinner();
        checkIntentData();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> handleSaveProduct());
    }

    private void verifyAdminProtection() {
        if (authViewModel.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        authViewModel.getUserProfileLiveData().observe(this, user -> {
            if (user == null || !Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
                Toast.makeText(AddEditFoodActivity.this, "Access Denied: Admin privileges required", Toast.LENGTH_LONG).show();
                authViewModel.logout();
                redirectToLogin();
            }
        });

        authViewModel.fetchUserProfile(authViewModel.getCurrentUser().getUid());
    }

    private void redirectToLogin() {
        Intent intent = new Intent(AddEditFoodActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupImageUrlWatcher() {
        binding.etImageUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    Glide.with(AddEditFoodActivity.this)
                            .load(url)
                            .placeholder(R.drawable.ic_food_placeholder)
                            .error(R.drawable.ic_food_placeholder)
                            .into(binding.ivProductPreview);
                } else {
                    binding.ivProductPreview.setImageResource(R.drawable.ic_food_placeholder);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategorySpinner() {
        categoriesList = new ArrayList<>();
        for (String cat : Constants.CATEGORIES) {
            if (!"All".equalsIgnoreCase(cat)) {
                categoriesList.add(cat);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoriesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void checkIntentData() {
        if (getIntent().hasExtra("FOOD_ITEM")) {
            editingFoodItem = (FoodItem) getIntent().getSerializableExtra("FOOD_ITEM");
            if (editingFoodItem != null) {
                binding.tvTitle.setText(R.string.edit_food_item);
                binding.etName.setText(editingFoodItem.getName());
                binding.etDescription.setText(editingFoodItem.getDescription());
                binding.etPrice.setText(String.valueOf(editingFoodItem.getPrice()));
                binding.etImageUrl.setText(editingFoodItem.getImageUrl() != null ? editingFoodItem.getImageUrl() : "");
                binding.switchAvailable.setChecked(editingFoodItem.isAvailable());
                binding.rbVeg.setChecked(editingFoodItem.isVeg());
                binding.rbNonVeg.setChecked(!editingFoodItem.isVeg());

                if (editingFoodItem.getCategory() != null) {
                    int index = categoriesList.indexOf(editingFoodItem.getCategory());
                    if (index >= 0) {
                        binding.spinnerCategory.setSelection(index);
                    }
                }

                if (editingFoodItem.getImageUrl() != null && !editingFoodItem.getImageUrl().isEmpty()) {
                    Glide.with(this)
                            .load(editingFoodItem.getImageUrl())
                            .placeholder(R.drawable.ic_food_placeholder)
                            .error(R.drawable.ic_food_placeholder)
                            .into(binding.ivProductPreview);
                }
            }
        }
    }

    private void handleSaveProduct() {
        String name = binding.etName.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        String imageUrl = binding.etImageUrl.getText().toString().trim();
        String category = categoriesList.get(binding.spinnerCategory.getSelectedItemPosition());
        boolean isAvailable = binding.switchAvailable.isChecked();
        boolean isVeg = binding.rbVeg.isChecked();

        if (name.isEmpty()) {
            binding.etName.setError("Name is required");
            return;
        }
        if (priceStr.isEmpty()) {
            binding.etPrice.setError("Price is required");
            return;
        }
        if (imageUrl.isEmpty()) {
            binding.etImageUrl.setError("Image URL is required");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            binding.etPrice.setError("Invalid price format");
            return;
        }

        saveToFirestore(name, description, price, category, imageUrl, isAvailable, isVeg);
    }

    private void saveToFirestore(String name, String description, double price, String category,
                                  String imageUrl, boolean isAvailable, boolean isVeg) {
        if (editingFoodItem != null) {
            editingFoodItem.setName(name);
            editingFoodItem.setDescription(description);
            editingFoodItem.setPrice(price);
            editingFoodItem.setCategory(category);
            editingFoodItem.setImageUrl(imageUrl);
            editingFoodItem.setAvailable(isAvailable);
            editingFoodItem.setVeg(isVeg);

            foodViewModel.updateFoodItem(editingFoodItem);
            Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            FoodItem newItem = new FoodItem(null, name, description, price, category, imageUrl, isAvailable);
            newItem.setVeg(isVeg);
            foodViewModel.addFoodItem(newItem);
            Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
