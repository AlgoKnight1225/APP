package com.canteen.foodordering.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.canteen.foodordering.R;
import com.canteen.foodordering.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> initialCategories, OnCategoryClickListener listener) {
        this.listener = listener;
        if (initialCategories != null) {
            this.categories.addAll(initialCategories);
        }
    }

    public void setCategories(List<Category> newCategories, String currentSelectedCategory) {
        this.categories.clear();
        if (newCategories != null) {
            this.categories.addAll(newCategories);
        }

        selectedPosition = 0;
        if (currentSelectedCategory != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getName().equalsIgnoreCase(currentSelectedCategory)) {
                    selectedPosition = i;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_chip, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvCategoryName.setText(category.getName());

        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_chip_selected);
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_dark));
            holder.ivCategoryIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_chip_unselected);
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
            holder.ivCategoryIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && pos < categories.size()) {
                selectedPosition = pos;
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onCategoryClick(categories.get(selectedPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        android.widget.ImageView ivCategoryIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
        }
    }
}
