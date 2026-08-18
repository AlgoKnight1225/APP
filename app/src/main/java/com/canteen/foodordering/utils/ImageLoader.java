package com.canteen.foodordering.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.canteen.foodordering.R;

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * Loads an image from a URL directly into the specified ImageView using Glide.
     * Sanitizes the URL, adds a browser User-Agent header, logs success/failure details,
     * and handles invalid or broken URLs with a fallback placeholder.
     *
     * @param imageView Target ImageView
     * @param rawUrl Image URL string saved by admin or fetched from database
     * @param placeholderResId Drawable resource to display while loading or on failure
     */
    public static void loadImage(ImageView imageView, String rawUrl, int placeholderResId) {
        if (imageView == null || imageView.getContext() == null) {
            return;
        }

        int defaultPlaceholder = placeholderResId > 0 ? placeholderResId : R.drawable.ic_food_placeholder;

        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            Log.w(TAG, "[Student Side] Received empty or null image URL. Using placeholder.");
            imageView.setImageResource(defaultPlaceholder);
            return;
        }

        String cleanUrl = rawUrl.trim();
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://" + cleanUrl;
        }

        Log.d(TAG, "[Student Side] Attempting to load image URL: " + cleanUrl);

        try {
            GlideUrl glideUrl = new GlideUrl(cleanUrl, new LazyHeaders.Builder()
                    .addHeader("User-Agent", USER_AGENT)
                    .build());

            Glide.with(imageView.getContext())
                    .load(glideUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(defaultPlaceholder)
                    .error(defaultPlaceholder)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "[Student Side] Failed to load image URL: " + model, e);
                            return false; // allow Glide to handle fallback error image
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "[Student Side] Successfully loaded image URL: " + model);
                            return false;
                        }
                    })
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "[Student Side] Exception initializing Glide for URL: " + cleanUrl, e);
            imageView.setImageResource(defaultPlaceholder);
        }
    }

    /**
     * Convenience method for food items using standard food placeholder.
     */
    public static void loadFoodImage(ImageView imageView, String rawUrl) {
        loadImage(imageView, rawUrl, R.drawable.ic_food_placeholder);
    }
}
