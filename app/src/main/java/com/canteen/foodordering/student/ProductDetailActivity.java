package com.canteen.foodordering.student;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ActivityProductDetailBinding;
import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.viewmodels.AuthViewModel;
import com.canteen.foodordering.viewmodels.CartViewModel;
import com.canteen.foodordering.viewmodels.FoodViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {
    private ActivityProductDetailBinding binding;
    private FoodViewModel foodViewModel;
    private CartViewModel cartViewModel;
    private AuthViewModel authViewModel;

    private FoodItem foodItem;
    private int quantity = 1;
    private double sizeExtraPrice = 0.0;
    private String selectedSize = "Regular";
    private double extraCheesePrice = 0.0;
    private double extraMushroomPrice = 0.0;
    private boolean isFavorite = false;
    private List<String> userFavoriteIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        if (getIntent().hasExtra("FOOD_ITEM")) {
            foodItem = (FoodItem) getIntent().getSerializableExtra("FOOD_ITEM");
        }

        if (foodItem == null) {
            Toast.makeText(this, "Item details unavailable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        setupListeners();
        observeFavorites();
    }

    private void setupViews() {
        binding.tvDetailName.setText(foodItem.getName());
        binding.tvDetailDescription.setText(foodItem.getDescription());
        binding.tvDetailRating.setText(String.valueOf(foodItem.getRating() > 0 ? foodItem.getRating() : 4.5));
        binding.tvDetailReviews.setText("(98 Reviews)");
        binding.tvBasePrice.setText(String.format("₹%.2f", foodItem.getPrice()));

        if (foodItem.getImageUrl() != null && !foodItem.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(foodItem.getImageUrl())
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .into(binding.ivDetailHeroImage);
        } else {
            binding.ivDetailHeroImage.setImageResource(R.drawable.ic_food_placeholder);
        }

        updateCalculatedPrice();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rgSize.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSizeMedium) {
                selectedSize = "Medium";
                sizeExtraPrice = 40.0;
            } else if (checkedId == R.id.rbSizeLarge) {
                selectedSize = "Large";
                sizeExtraPrice = 80.0;
            } else {
                selectedSize = "Regular";
                sizeExtraPrice = 0.0;
            }
            updateCalculatedPrice();
        });

        binding.cbExtraCheese.setOnCheckedChangeListener((buttonView, isChecked) -> {
            extraCheesePrice = isChecked ? 20.0 : 0.0;
            updateCalculatedPrice();
        });

        binding.cbExtraMushroom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            extraMushroomPrice = isChecked ? 20.0 : 0.0;
            updateCalculatedPrice();
        });

        binding.btnDecreaseQty.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
                updateCalculatedPrice();
            }
        });

        binding.btnIncreaseQty.setOnClickListener(v -> {
            quantity++;
            binding.tvQuantity.setText(String.valueOf(quantity));
            updateCalculatedPrice();
        });

        binding.btnAddToCart.setOnClickListener(v -> handleAddToCart());

        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void observeFavorites() {
        authViewModel.getUserProfileLiveData().observe(this, user -> {
            if (user != null) {
                userFavoriteIds = user.getFavoriteIds();
                isFavorite = userFavoriteIds.contains(foodItem.getId());
                updateFavoriteIcon();
            }
        });

        if (authViewModel.getCurrentUser() != null) {
            authViewModel.fetchUserProfile(authViewModel.getCurrentUser().getUid());
        }
    }

    private void toggleFavorite() {
        foodViewModel.toggleFavorite(foodItem.getId(), isFavorite);
        isFavorite = !isFavorite;
        updateFavoriteIcon();
        Toast.makeText(this, isFavorite ? "Added to Favorites!" : "Removed from Favorites", Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void updateCalculatedPrice() {
        double unitPrice = foodItem.getPrice() + sizeExtraPrice + extraCheesePrice + extraMushroomPrice;
        double totalPrice = unitPrice * quantity;
        binding.tvTotalPrice.setText(String.format("₹%.2f", totalPrice));
    }

    private void handleAddToCart() {
        double unitPrice = foodItem.getPrice() + sizeExtraPrice + extraCheesePrice + extraMushroomPrice;
        
        StringBuilder customizations = new StringBuilder(selectedSize);
        if (extraCheesePrice > 0) customizations.append(", Extra Cheese");
        if (extraMushroomPrice > 0) customizations.append(", Extra Mushroom");

        String customizedName = foodItem.getName() + " (" + customizations.toString() + ")";

        // Each CartItem gets its own unique cartItemId (UUID) — never overwrites previous items
        CartItem cartItem = new CartItem(
                foodItem.getId(),
                customizedName,
                unitPrice,
                quantity,
                foodItem.getImageUrl()
        );

        // Save to Firebase as a separate document — not in-memory CartManager
        cartViewModel.addPendingCartItem(cartItem);
        Toast.makeText(this, foodItem.getName() + " saved to Cart! 🛒", Toast.LENGTH_SHORT).show();
        finish();
    }
}
