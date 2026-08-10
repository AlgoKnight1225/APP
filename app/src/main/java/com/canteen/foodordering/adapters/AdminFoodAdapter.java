package com.canteen.foodordering.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ItemFoodAdminBinding;
import com.canteen.foodordering.models.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class AdminFoodAdapter extends RecyclerView.Adapter<AdminFoodAdapter.AdminFoodViewHolder> {
    private List<FoodItem> foodList = new ArrayList<>();
    private final OnAdminFoodActionListener listener;

    public interface OnAdminFoodActionListener {
        void onEditClick(FoodItem foodItem);
        void onDeleteClick(FoodItem foodItem);
        void onAvailabilityToggle(FoodItem foodItem, boolean isAvailable);
    }

    public AdminFoodAdapter(OnAdminFoodActionListener listener) {
        this.listener = listener;
    }

    public void setFoodList(List<FoodItem> list) {
        this.foodList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodAdminBinding binding = ItemFoodAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminFoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminFoodViewHolder holder, int position) {
        FoodItem item = foodList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class AdminFoodViewHolder extends RecyclerView.ViewHolder {
        private final ItemFoodAdminBinding binding;

        public AdminFoodViewHolder(@NonNull ItemFoodAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FoodItem item, OnAdminFoodActionListener listener) {
            binding.tvAdminFoodName.setText(item.getName());
            binding.tvAdminCategory.setText(item.getCategory() != null ? item.getCategory() : "Food");
            binding.tvAdminFoodPrice.setText(String.format("₹%.2f", item.getPrice()));
            binding.switchAvailable.setChecked(item.isAvailable());

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_food_placeholder)
                        .error(R.drawable.ic_food_placeholder)
                        .into(binding.ivAdminFood);
            } else {
                binding.ivAdminFood.setImageResource(R.drawable.ic_food_placeholder);
            }

            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(item);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item);
                }
            });

            binding.switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null && buttonView.isPressed()) {
                    listener.onAvailabilityToggle(item, isChecked);
                }
            });
        }
    }
}
