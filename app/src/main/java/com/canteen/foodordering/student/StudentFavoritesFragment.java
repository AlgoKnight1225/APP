package com.canteen.foodordering.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.canteen.foodordering.adapters.StudentFoodAdapter;
import com.canteen.foodordering.databinding.FragmentStudentFavoritesBinding;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.viewmodels.AuthViewModel;
import com.canteen.foodordering.viewmodels.CartViewModel;
import com.canteen.foodordering.viewmodels.FoodViewModel;

import java.util.ArrayList;
import java.util.List;

public class StudentFavoritesFragment extends Fragment implements StudentFoodAdapter.OnFoodItemClickListener {
    private FragmentStudentFavoritesBinding binding;
    private FoodViewModel foodViewModel;
    private CartViewModel cartViewModel;
    private AuthViewModel authViewModel;
    private StudentFoodAdapter foodAdapter;

    private List<FoodItem> allFoodItems = new ArrayList<>();
    private List<String> favoriteIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        foodViewModel = new ViewModelProvider(requireActivity()).get(FoodViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        foodAdapter = new StudentFoodAdapter(this);
        binding.rvFavorites.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvFavorites.setAdapter(foodAdapter);

        observeData();
    }

    private void observeData() {
        authViewModel.getUserProfileLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                favoriteIds = user.getFavoriteIds();
                updateFavoritesList();
            }
        });

        foodViewModel.getFoodItemsLiveData().observe(getViewLifecycleOwner(), items -> {
            allFoodItems = items != null ? items : new ArrayList<>();
            updateFavoritesList();
        });
    }

    private void updateFavoritesList() {
        List<FoodItem> favorites = new ArrayList<>();
        for (FoodItem item : allFoodItems) {
            if (favoriteIds.contains(item.getId())) {
                item.setFavorite(true);
                favorites.add(item);
            }
        }
        foodAdapter.setFoodList(favorites);
        binding.layoutEmpty.setVisibility(favorites.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAddToCartClick(FoodItem foodItem) {
        cartViewModel.addItem(foodItem);
        Toast.makeText(requireContext(), foodItem.getName() + " added to cart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteClick(FoodItem foodItem) {
        foodViewModel.toggleFavorite(foodItem.getId(), true);
        favoriteIds.remove(foodItem.getId());
        updateFavoritesList();
        Toast.makeText(requireContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
