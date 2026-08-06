package com.canteen.foodordering.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.AdminFoodAdapter;
import com.canteen.foodordering.databinding.FragmentAdminFoodBinding;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminFoodListFragment extends Fragment {

    private FragmentAdminFoodBinding binding;
    private FirebaseFirestore db;
    private final List<FoodItem> foodList = new ArrayList<>();
    private AdminFoodAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminFoodBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        setupRecyclerView();

        binding.fabAddFood.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddEditFoodActivity.class));
        });

        fetchFoodItems();
    }

    private void setupRecyclerView() {
        adapter = new AdminFoodAdapter(requireContext(), foodList, new AdminFoodAdapter.OnFoodActionListener() {
            @Override
            public void onEdit(FoodItem foodItem) {
                Intent intent = new Intent(requireContext(), AddEditFoodActivity.class);
                intent.putExtra("food_item", foodItem);
                startActivity(intent);
            }

            @Override
            public void onDelete(FoodItem foodItem) {
                showDeleteConfirmation(foodItem);
            }

            @Override
            public void onToggleAvailability(FoodItem foodItem, boolean isAvailable) {
                updateAvailability(foodItem, isAvailable);
            }
        });

        binding.rvAdminFood.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminFood.setAdapter(adapter);
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
                        foodList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            FoodItem item = doc.toObject(FoodItem.class);
                            item.setId(doc.getId());
                            foodList.add(item);
                        }
                        adapter.notifyDataSetChanged();

                        if (foodList.isEmpty()) {
                            binding.tvEmptyAdminFood.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvEmptyAdminFood.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void updateAvailability(FoodItem foodItem, boolean isAvailable) {
        if (foodItem.getId() == null) return;
        db.collection(Constants.COLLECTION_FOOD)
                .document(foodItem.getId())
                .update("available", isAvailable)
                .addOnSuccessListener(aVoid -> {
                    foodItem.setAvailable(isAvailable);
                    Toast.makeText(requireContext(), "Availability updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmation(FoodItem foodItem) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Food Item")
                .setMessage("Are you sure you want to delete '" + foodItem.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteFoodItem(foodItem);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFoodItem(FoodItem foodItem) {
        if (foodItem.getId() == null) return;
        db.collection(Constants.COLLECTION_FOOD)
                .document(foodItem.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error deleting item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
