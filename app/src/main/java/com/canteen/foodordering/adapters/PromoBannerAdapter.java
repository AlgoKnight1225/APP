package com.canteen.foodordering.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ItemPromoBannerBinding;
import com.canteen.foodordering.models.PromoBanner;

import java.util.List;

public class PromoBannerAdapter extends RecyclerView.Adapter<PromoBannerAdapter.BannerViewHolder> {
    private final List<PromoBanner> bannerList;

    public PromoBannerAdapter(List<PromoBanner> bannerList) {
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPromoBannerBinding binding = ItemPromoBannerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BannerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        PromoBanner banner = bannerList.get(position);
        holder.bind(banner);
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ItemPromoBannerBinding binding;

        public BannerViewHolder(@NonNull ItemPromoBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PromoBanner banner) {
            binding.tvPromoTitle.setText(banner.getTitle());
            binding.tvPromoSubtitle.setText(banner.getSubtitle());
            binding.tvPromoBadge.setText(banner.getPromoCode() != null ? banner.getPromoCode() : "SPECIAL OFFER");

            if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(banner.getImageUrl())
                        .placeholder(R.drawable.ic_restaurant_menu)
                        .into(binding.ivPromoImage);
            }
        }
    }
}
