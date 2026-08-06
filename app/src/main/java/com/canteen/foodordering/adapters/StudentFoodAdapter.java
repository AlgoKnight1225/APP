package com.canteen.foodordering.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canteen.foodordering.R;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.utils.CartManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class StudentFoodAdapter extends RecyclerView.Adapter<StudentFoodAdapter.ViewHolder> {

    private final Context context;
    private final List<FoodItem> foodList;
    private final OnCartUpdatedListener listener;

    public interface OnCartUpdatedListener {
        void onCartUpdated();
    }

    public StudentFoodAdapter(Context context, List<FoodItem> foodList, OnCartUpdatedListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = foodList.get(position);

        holder.tvFoodName.setText(item.getName());
        holder.tvFoodDescription.setText(item.getDescription());
        holder.tvCategory.setText(item.getCategory());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₹ %.2f", item.getPrice()));

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .into(holder.ivFoodImage);
        } else {
            holder.ivFoodImage.setImageResource(R.drawable.ic_food_placeholder);
        }

        if (item.isAvailable()) {
            holder.btnAddToCart.setVisibility(View.VISIBLE);
            holder.tvUnavailable.setVisibility(View.GONE);
            holder.btnAddToCart.setOnClickListener(v -> {
                CartManager.getInstance().addItem(item);
                Toast.makeText(context, item.getName() + " added to cart", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onCartUpdated();
                }
            });
        } else {
            holder.btnAddToCart.setVisibility(View.GONE);
            holder.tvUnavailable.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvCategory, tvFoodName, tvFoodDescription, tvPrice, tvUnavailable;
        MaterialButton btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDescription = itemView.findViewById(R.id.tvFoodDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvUnavailable = itemView.findViewById(R.id.tvUnavailable);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
