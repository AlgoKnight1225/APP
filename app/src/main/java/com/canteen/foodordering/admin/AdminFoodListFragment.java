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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.AdminFoodAdapter;
import com.canteen.foodordering.databinding.FragmentAdminFoodBinding;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.viewmodels.FoodViewModel;

import java.util.ArrayList;

public class AdminFoodListFragment extends Fragment implements AdminFoodAdapter.OnAdminFoodActionListener {
    private FragmentAdminFoodBinding binding;
    private FoodViewModel foodViewModel;
    private AdminFoodAdapter foodAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminFoodBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        foodViewModel = new ViewModelProvider(requireActivity()).get(FoodViewModel.class);

        foodAdapter = new AdminFoodAdapter(this);
        binding.rvAdminFood.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminFood.setAdapter(foodAdapter);

        observeFoodItems();

        binding.fabAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddEditFoodActivity.class);
            startActivity(intent);
        });
    }

    private void observeFoodItems() {
        binding.progressBar.setVisibility(View.VISIBLE);

        foodViewModel.getFoodItemsLiveData().observe(getViewLifecycleOwner(), items -> {
            binding.progressBar.setVisibility(View.GONE);
            if (items != null && !items.isEmpty()) {
                foodAdapter.setFoodList(items);
                binding.layoutEmpty.setVisibility(View.GONE);
            } else {
                foodAdapter.setFoodList(new ArrayList<>());
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onEditClick(FoodItem foodItem) {
        Intent intent = new Intent(requireActivity(), AddEditFoodActivity.class);
        intent.putExtra("FOOD_ITEM", foodItem);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(FoodItem foodItem) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Food Item")
                .setMessage("Are you sure you want to delete '" + foodItem.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    foodViewModel.deleteFoodItem(foodItem.getId());
                    Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAvailabilityToggle(FoodItem foodItem, boolean isAvailable) {
        foodItem.setAvailable(isAvailable);
        foodViewModel.updateFoodItem(foodItem);
        Toast.makeText(requireContext(), foodItem.getName() + (isAvailable ? " is now Available" : " marked Unavailable"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
