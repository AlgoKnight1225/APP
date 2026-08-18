package com.canteen.foodordering.student;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.CategoryAdapter;
import com.canteen.foodordering.adapters.PromoBannerAdapter;
import com.canteen.foodordering.adapters.StudentFoodAdapter;
import com.canteen.foodordering.databinding.FragmentStudentHomeBinding;
import com.canteen.foodordering.models.Category;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.models.PromoBanner;
import com.canteen.foodordering.utils.Constants;
import com.canteen.foodordering.viewmodels.AuthViewModel;
import com.canteen.foodordering.viewmodels.CartViewModel;
import com.canteen.foodordering.viewmodels.FoodViewModel;

import java.util.ArrayList;
import java.util.List;

public class StudentHomeFragment extends Fragment implements StudentFoodAdapter.OnFoodItemClickListener {
    private FragmentStudentHomeBinding binding;
    private FoodViewModel foodViewModel;
    private CartViewModel cartViewModel;
    private AuthViewModel authViewModel;
    private StudentFoodAdapter foodAdapter;
    private CategoryAdapter categoryAdapter;
    private PromoBannerAdapter promoBannerAdapter;

    private List<FoodItem> allFoodItems = new ArrayList<>();
    private List<String> userFavoriteIds = new ArrayList<>();
    private String selectedCategory = "All";
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        foodViewModel = new ViewModelProvider(requireActivity()).get(FoodViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupPromoBanners();
        setupCategoriesRecyclerView();
        setupFoodRecyclerView();
        setupSearch();
        observeViewModels();

        binding.fabCart.setOnClickListener(v -> {
            startActivity(new android.content.Intent(requireContext(), CartActivity.class));
        });
    }

    private void setupPromoBanners() {
        List<PromoBanner> banners = new ArrayList<>();
        banners.add(new PromoBanner("20% OFF Morning Brews", "Special discount on all cold coffee & lattes", "BREW20", ""));
        banners.add(new PromoBanner("Combo Meal Offer", "Buy any Sandwich & Get Iced Tea at ₹49", "MEALCOMBO", ""));
        banners.add(new PromoBanner("Fresh Artisanal Bakery", "Freshly baked croissants & cookies daily", "FRESHBAKE", ""));

        promoBannerAdapter = new PromoBannerAdapter(banners);
        binding.rvPromoBanners.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPromoBanners.setAdapter(promoBannerAdapter);
    }

    private List<Category> getInitialCategoryList() {
        List<Category> categoryList = new ArrayList<>();
        for (int i = 0; i < Constants.CATEGORIES.length; i++) {
            categoryList.add(new Category(String.valueOf(i), Constants.CATEGORIES[i], i == 0));
        }
        return categoryList;
    }

    private void setupCategoriesRecyclerView() {
        categoryAdapter = new CategoryAdapter(getInitialCategoryList(), category -> {
            selectedCategory = category.getName();
            filterAndDisplayItems();
        });
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void updateCategoriesList(List<FoodItem> foodItems) {
        List<String> dynamicCategories = new ArrayList<>();
        dynamicCategories.add("All");

        // Add standard categories from Constants (which matches Admin)
        for (String cat : Constants.CATEGORIES) {
            if (!"All".equalsIgnoreCase(cat) && !containsCategory(dynamicCategories, cat)) {
                dynamicCategories.add(cat);
            }
        }

        // Dynamically add any additional categories created in food items
        if (foodItems != null) {
            for (FoodItem item : foodItems) {
                if (item.getCategory() != null && !item.getCategory().trim().isEmpty()) {
                    String cat = item.getCategory().trim();
                    if (!containsCategory(dynamicCategories, cat)) {
                        dynamicCategories.add(cat);
                    }
                }
            }
        }

        List<Category> categoryList = new ArrayList<>();
        for (int i = 0; i < dynamicCategories.size(); i++) {
            String catName = dynamicCategories.get(i);
            boolean isSelected = catName.equalsIgnoreCase(selectedCategory);
            categoryList.add(new Category(String.valueOf(i), catName, isSelected));
        }

        if (categoryAdapter != null) {
            categoryAdapter.setCategories(categoryList, selectedCategory);
        }
    }

    private boolean containsCategory(List<String> list, String value) {
        if (list == null || value == null) return false;
        for (String item : list) {
            if (item != null && (item.equalsIgnoreCase(value) || isCategoryMatch(item, value))) {
                return true;
            }
        }
        return false;
    }

    private void setupFoodRecyclerView() {
        foodAdapter = new StudentFoodAdapter(this);
        binding.rvFoodItems.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvFoodItems.setNestedScrollingEnabled(false);
        binding.rvFoodItems.setAdapter(foodAdapter);
    }

    private void setupSearch() {
        binding.etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                filterAndDisplayItems();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModels() {
        binding.progressBar.setVisibility(View.VISIBLE);

        authViewModel.getUserProfileLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvStudentHeaderName.setText(user.getName() != null ? user.getName() : "FIRST BITE");
                userFavoriteIds = user.getFavoriteIds();
                filterAndDisplayItems();
            }
        });

        if (authViewModel.getCurrentUser() != null) {
            authViewModel.fetchUserProfile(authViewModel.getCurrentUser().getUid());
        }

        foodViewModel.getFoodItemsLiveData().observe(getViewLifecycleOwner(), items -> {
            binding.progressBar.setVisibility(View.GONE);
            allFoodItems = items != null ? items : new ArrayList<>();
            updateCategoriesList(allFoodItems);
            filterAndDisplayItems();
        });
    }

    private boolean isCategoryMatch(String itemCategory, String targetCategory) {
        if (itemCategory == null || targetCategory == null) return false;
        String item = itemCategory.trim().toLowerCase();
        String target = targetCategory.trim().toLowerCase();

        if (item.equals(target)) return true;

        // Handle "Dessert" vs "Desserts"
        if ((target.equals("dessert") && item.equals("desserts")) ||
            (target.equals("desserts") && item.equals("dessert"))) {
            return true;
        }

        // Handle "Coffee & Drinks" matching "Drinks", "Coffee", "Beverages"
        if (target.contains("coffee") && (target.contains("drinks") || target.contains("drink"))) {
            if (item.contains("coffee") || item.contains("drink") || item.contains("beverage")) {
                return true;
            }
        }

        // Handle "Snacks & Bakery" matching "Snacks", "Bakery"
        if (target.contains("snacks") && target.contains("bakery")) {
            if (item.contains("snack") || item.contains("bakery")) {
                return true;
            }
        }

        return false;
    }

    private void filterAndDisplayItems() {
        List<FoodItem> filtered = new ArrayList<>();
        for (FoodItem item : allFoodItems) {
            if (!item.isAvailable()) continue;

            boolean matchesCategory = "All".equalsIgnoreCase(selectedCategory) ||
                    isCategoryMatch(item.getCategory(), selectedCategory);

            boolean matchesSearch = searchQuery.isEmpty() ||
                    (item.getName() != null && item.getName().toLowerCase().contains(searchQuery)) ||
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchQuery));

            if (matchesCategory && matchesSearch) {
                item.setFavorite(userFavoriteIds.contains(item.getId()));
                filtered.add(item);
            }
        }

        foodAdapter.setFoodList(filtered);
        if (filtered.isEmpty()) {
            if (!"All".equalsIgnoreCase(selectedCategory)) {
                binding.layoutEmpty.setText("No food items available in \"" + selectedCategory + "\".");
            } else if (!searchQuery.isEmpty()) {
                binding.layoutEmpty.setText("No food items matching \"" + searchQuery + "\".");
            } else {
                binding.layoutEmpty.setText("No food items available at this time.");
            }
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(FoodItem foodItem) {
        if (foodItem != null && getContext() != null) {
            android.content.Intent intent = new android.content.Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("FOOD_ITEM", foodItem);
            startActivity(intent);
        }
    }

    @Override
    public void onAddToCartClick(FoodItem foodItem) {
        cartViewModel.addItem(foodItem);
        Toast.makeText(requireContext(), foodItem.getName() + " added to cart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteClick(FoodItem foodItem) {
        boolean isFav = userFavoriteIds.contains(foodItem.getId());
        foodViewModel.toggleFavorite(foodItem.getId(), isFav);
        if (isFav) {
            userFavoriteIds.remove(foodItem.getId());
            Toast.makeText(requireContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
        } else {
            userFavoriteIds.add(foodItem.getId());
            Toast.makeText(requireContext(), "Added to Favorites!", Toast.LENGTH_SHORT).show();
        }
        filterAndDisplayItems();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
