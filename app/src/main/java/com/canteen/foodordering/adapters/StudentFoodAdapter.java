package com.canteen.foodordering.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ItemFoodStudentBinding;
import com.canteen.foodordering.models.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class StudentFoodAdapter extends RecyclerView.Adapter<StudentFoodAdapter.FoodViewHolder> {
    private List<FoodItem> foodList = new ArrayList<>();
    private final OnFoodItemClickListener listener;

    public interface OnFoodItemClickListener {
        void onAddToCartClick(FoodItem foodItem);
        void onFavoriteClick(FoodItem foodItem);
        void onItemClick(FoodItem foodItem);
    }

    public StudentFoodAdapter(OnFoodItemClickListener listener) {
        this.listener = listener;
    }

    public void setFoodList(List<FoodItem> list) {
        this.foodList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodStudentBinding binding = ItemFoodStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        private final ItemFoodStudentBinding binding;

        public FoodViewHolder(@NonNull ItemFoodStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FoodItem item, OnFoodItemClickListener listener) {
            binding.tvFoodName.setText(item.getName());
            binding.tvFoodDescription.setText(item.getDescription());
            binding.tvFoodCategory.setText(item.getCategory() != null ? item.getCategory() : "Food");
            binding.tvFoodPrice.setText(String.format("₹%.2f", item.getPrice()));
            binding.tvRating.setText(String.valueOf(item.getRating() > 0 ? item.getRating() : 4.5));

            // Veg / Non-Veg badge
            if (item.isVeg()) {
                binding.ivVegIndicator.setImageResource(R.drawable.ic_veg);
            } else {
                binding.ivVegIndicator.setImageResource(R.drawable.ic_non_veg);
            }

            // Discount badge
            if (item.getDiscountPercent() > 0) {
                binding.tvDiscountBadge.setVisibility(View.VISIBLE);
                binding.tvDiscountBadge.setText(item.getDiscountPercent() + "% OFF");
            } else {
                binding.tvDiscountBadge.setVisibility(View.GONE);
            }

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_food_placeholder)
                        .error(R.drawable.ic_food_placeholder)
                        .into(binding.ivFoodImage);
            } else {
                binding.ivFoodImage.setImageResource(R.drawable.ic_food_placeholder);
            }

            if (item.isFavorite()) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            binding.btnAddToCart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClick(item);
                }
            });

            binding.btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(item);
                }
            });
        }
    }
}
