package com.canteen.foodordering.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ActivityAddEditFoodBinding;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;

public class AddEditFoodActivity extends AppCompatActivity {

    private ActivityAddEditFoodBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;
    private FoodItem existingFoodItem = null;
    private boolean isEditMode = false;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivFoodPreview.setImageURI(uri);
                    binding.layoutTapToSelect.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        setSupportActionBar(binding.toolbarAddEdit);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarAddEdit.setNavigationOnClickListener(v -> finish());

        setupCategorySpinner();

        if (getIntent().hasExtra("food_item")) {
            existingFoodItem = (FoodItem) getIntent().getSerializableExtra("food_item");
            if (existingFoodItem != null) {
                isEditMode = true;
                populateExistingData();
            }
        }

        binding.cardSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnSaveFood.setOnClickListener(v -> saveFoodItem());
    }

    private void setupCategorySpinner() {
        // Exclude "All" from creation options
        String[] categories = Arrays.copyOfRange(Constants.CATEGORIES, 1, Constants.CATEGORIES.length);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        binding.spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void populateExistingData() {
        binding.toolbarAddEdit.setTitle(R.string.edit_food_item);
        binding.etFoodName.setText(existingFoodItem.getName());
        binding.etDescription.setText(existingFoodItem.getDescription());
        binding.etPrice.setText(String.valueOf(existingFoodItem.getPrice()));
        binding.switchIsAvailable.setChecked(existingFoodItem.isAvailable());

        if (existingFoodItem.getCategory() != null) {
            String[] categories = Arrays.copyOfRange(Constants.CATEGORIES, 1, Constants.CATEGORIES.length);
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equalsIgnoreCase(existingFoodItem.getCategory())) {
                    binding.spinnerCategory.setSelection(i);
                    break;
                }
            }
        }

        if (existingFoodItem.getImageUrl() != null && !existingFoodItem.getImageUrl().isEmpty()) {
            binding.layoutTapToSelect.setVisibility(View.GONE);
            Glide.with(this)
                    .load(existingFoodItem.getImageUrl())
                    .placeholder(R.drawable.ic_food_placeholder)
                    .into(binding.ivFoodPreview);
        }
    }

    private void saveFoodItem() {
        String name = binding.etFoodName.getText() != null ? binding.etFoodName.getText().toString().trim() : "";
        String description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        String priceStr = binding.etPrice.getText() != null ? binding.etPrice.getText().toString().trim() : "";
        String category = binding.spinnerCategory.getSelectedItem() != null ? binding.spinnerCategory.getSelectedItem().toString() : "Snacks";
        boolean isAvailable = binding.switchIsAvailable.isChecked();

        if (TextUtils.isEmpty(name)) {
            binding.tilFoodName.setError("Item name is required");
            return;
        } else {
            binding.tilFoodName.setError(null);
        }

        if (TextUtils.isEmpty(priceStr)) {
            binding.tilPrice.setError("Price is required");
            return;
        } else {
            binding.tilPrice.setError(null);
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            binding.tilPrice.setError("Enter a valid price amount");
            return;
        }

        showLoading(true);

        if (selectedImageUri != null) {
            // Upload image first
            uploadImageAndSaveFood(name, description, price, category, isAvailable);
        } else {
            String imageUrl = isEditMode && existingFoodItem != null ? existingFoodItem.getImageUrl() : "";
            saveToFirestore(name, description, price, category, imageUrl, isAvailable);
        }
    }

    private void uploadImageAndSaveFood(String name, String description, double price, String category, boolean isAvailable) {
        String fileName = "food_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storage.getReference().child("food_images/" + fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveToFirestore(name, description, price, category, uri.toString(), isAvailable);
                }))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(AddEditFoodActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveToFirestore(String name, String description, double price, String category, String imageUrl, boolean isAvailable) {
        String docId = isEditMode && existingFoodItem != null ? existingFoodItem.getId() : db.collection(Constants.COLLECTION_FOOD).document().getId();

        FoodItem item = new FoodItem(docId, name, description, price, category, imageUrl, isAvailable);

        db.collection(Constants.COLLECTION_FOOD)
                .document(docId)
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(AddEditFoodActivity.this, isEditMode ? "Food item updated!" : "Food item added!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(AddEditFoodActivity.this, "Failed to save item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSaveFood.setEnabled(!isLoading);
    }
}
