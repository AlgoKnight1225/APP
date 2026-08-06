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
import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.utils.CartManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;
    private final List<CartItem> cartList;
    private final OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public CartAdapter(Context context, List<CartItem> cartList, OnCartChangeListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartList.get(position);

        holder.tvCartItemName.setText(item.getFoodName());
        holder.tvCartItemPrice.setText(String.format(Locale.getDefault(), "₹ %.2f each", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvItemTotalPrice.setText(String.format(Locale.getDefault(), "₹ %.2f", item.getTotalPrice()));

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .into(holder.ivCartItemImage);
        } else {
            holder.ivCartItemImage.setImageResource(R.drawable.ic_food_placeholder);
        }

        holder.btnPlus.setOnClickListener(v -> {
            CartManager.getInstance().updateQuantity(item.getFoodId(), item.getQuantity() + 1);
            notifyDataSetChanged();
            if (listener != null) listener.onCartChanged();
        });

        holder.btnMinus.setOnClickListener(v -> {
            CartManager.getInstance().updateQuantity(item.getFoodId(), item.getQuantity() - 1);
            notifyDataSetChanged();
            if (listener != null) listener.onCartChanged();
        });

        holder.btnRemove.setOnClickListener(v -> {
            CartManager.getInstance().removeItem(item.getFoodId());
            notifyDataSetChanged();
            if (listener != null) listener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCartItemImage;
        TextView tvCartItemName, tvCartItemPrice, tvQuantity, tvItemTotalPrice;
        MaterialButton btnMinus, btnPlus;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCartItemImage = itemView.findViewById(R.id.ivCartItemImage);
            tvCartItemName = itemView.findViewById(R.id.tvCartItemName);
            tvCartItemPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvItemTotalPrice = itemView.findViewById(R.id.tvItemTotalPrice);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
