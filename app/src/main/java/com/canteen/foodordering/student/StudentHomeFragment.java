package com.canteen.foodordering.student;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.R;
import com.canteen.foodordering.adapters.StudentFoodAdapter;
import com.canteen.foodordering.databinding.FragmentStudentHomeBinding;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.models.User;
import com.canteen.foodordering.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentHomeFragment extends Fragment {

    private FragmentStudentHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private final List<FoodItem> allFoodList = new ArrayList<>();
    private final List<FoodItem> filteredList = new ArrayList<>();
    private StudentFoodAdapter adapter;
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

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        setupCategoryChips();
        setupSearch();
        fetchUserProfile();
        fetchFoodItems();

        binding.fabCart.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CartActivity.class));
        });
    }

    private void setupRecyclerView() {
        adapter = new StudentFoodAdapter(requireContext(), filteredList, () -> {
            // Callback when items added to cart
        });
        binding.rvFoodItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFoodItems.setAdapter(adapter);
    }

    private void fetchUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection(Constants.COLLECTION_USERS)
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && isAdded()) {
                            User u = documentSnapshot.toObject(User.class);
                            if (u != null && u.getName() != null) {
                                binding.tvWelcomeUser.setText(String.format("Hello, %s!", u.getName().split(" ")[0]));
                            }
                        }
                    });
        }
    }

    private void setupCategoryChips() {
        binding.chipGroupCategory.removeAllViews();
        for (String cat : Constants.CATEGORIES) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setClickable(true);
            if (cat.equalsIgnoreCase("All")) {
                chip.setChecked(true);
            }
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategory = cat;
                    applyFilters();
                }
            });
            binding.chipGroupCategory.addView(chip);
        }
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchFoodItems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.COLLECTION_FOOD)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(requireContext(), "Error fetching menu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        allFoodList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            FoodItem item = doc.toObject(FoodItem.class);
                            item.setId(doc.getId());
                            allFoodList.add(item);
                        }
                        applyFilters();
                    }
                });
    }

    private void applyFilters() {
        filteredList.clear();
        for (FoodItem item : allFoodList) {
            boolean matchesCategory = selectedCategory.equalsIgnoreCase("All") ||
                    (item.getCategory() != null && item.getCategory().equalsIgnoreCase(selectedCategory));

            boolean matchesSearch = searchQuery.isEmpty() ||
                    (item.getName() != null && item.getName().toLowerCase().contains(searchQuery.toLowerCase()));

            if (matchesCategory && matchesSearch) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
