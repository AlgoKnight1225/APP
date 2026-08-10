package com.canteen.foodordering.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ItemCartBinding;
import com.canteen.foodordering.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems = new ArrayList<>();
    private final OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged(String foodId, int newQuantity);
        void onItemRemoved(String foodId);
    }

    public CartAdapter(OnCartItemChangeListener listener) {
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> list) {
        this.cartItems = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        public CartViewHolder(@NonNull ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartItem item, OnCartItemChangeListener listener) {
            binding.tvCartFoodName.setText(item.getFoodName());
            binding.tvCartFoodPrice.setText(String.format("₹%.2f", item.getTotalPrice()));
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_food_placeholder)
                        .error(R.drawable.ic_food_placeholder)
                        .into(binding.ivCartFood);
            } else {
                binding.ivCartFood.setImageResource(R.drawable.ic_food_placeholder);
            }

            binding.btnPlus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(item.getFoodId(), item.getQuantity() + 1);
                }
            });

            binding.btnMinus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(item.getFoodId(), item.getQuantity() - 1);
                }
            });

            if (binding.btnDelete != null) {
                binding.btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemRemoved(item.getFoodId());
                    }
                });
            }
        }
    }
}
