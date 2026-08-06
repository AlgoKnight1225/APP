package com.canteen.foodordering.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canteen.foodordering.R;
import com.canteen.foodordering.models.FoodItem;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;
import java.util.Locale;

public class AdminFoodAdapter extends RecyclerView.Adapter<AdminFoodAdapter.ViewHolder> {

    private final Context context;
    private final List<FoodItem> foodList;
    private final OnFoodActionListener listener;

    public interface OnFoodActionListener {
        void onEdit(FoodItem foodItem);
        void onDelete(FoodItem foodItem);
        void onToggleAvailability(FoodItem foodItem, boolean isAvailable);
    }

    public AdminFoodAdapter(Context context, List<FoodItem> foodList, OnFoodActionListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = foodList.get(position);

        holder.tvAdminFoodName.setText(item.getName());
        holder.tvAdminCategory.setText(item.getCategory());
        holder.tvAdminPrice.setText(String.format(Locale.getDefault(), "₹ %.2f", item.getPrice()));
        
        // Prevent trigger loop during recycling
        holder.switchAvailable.setOnCheckedChangeListener(null);
        holder.switchAvailable.setChecked(item.isAvailable());

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .into(holder.ivAdminFoodImage);
        } else {
            holder.ivAdminFoodImage.setImageResource(R.drawable.ic_food_placeholder);
        }

        holder.switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleAvailability(item, isChecked);
            }
        });

        holder.btnEditFood.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });

        holder.btnDeleteFood.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAdminFoodImage;
        TextView tvAdminCategory, tvAdminFoodName, tvAdminPrice;
        SwitchMaterial switchAvailable;
        ImageButton btnEditFood, btnDeleteFood;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAdminFoodImage = itemView.findViewById(R.id.ivAdminFoodImage);
            tvAdminCategory = itemView.findViewById(R.id.tvAdminCategory);
            tvAdminFoodName = itemView.findViewById(R.id.tvAdminFoodName);
            tvAdminPrice = itemView.findViewById(R.id.tvAdminPrice);
            switchAvailable = itemView.findViewById(R.id.switchAvailable);
            btnEditFood = itemView.findViewById(R.id.btnEditFood);
            btnDeleteFood = itemView.findViewById(R.id.btnDeleteFood);
        }
    }
}
