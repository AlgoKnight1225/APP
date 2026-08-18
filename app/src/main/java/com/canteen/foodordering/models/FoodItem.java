package com.canteen.foodordering.models;

import java.io.Serializable;

public class FoodItem implements Serializable {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;
    private boolean available;
    private double rating;
    private String prepTime;
    private boolean isFavorite;
    private boolean isVeg;
    private int discountPercent;

    // Required empty constructor for Firestore
    public FoodItem() {
        this.available = true;
        this.rating = 4.5;
        this.prepTime = "10 mins";
        this.isFavorite = false;
        this.isVeg = true;
        this.discountPercent = 0;
    }

    public FoodItem(String id, String name, String description, double price, String category, String imageUrl, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.available = available;
        this.rating = 4.5;
        this.prepTime = "10-15 mins";
        this.isFavorite = false;
        this.isVeg = true;
        this.discountPercent = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @com.google.firebase.firestore.PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @com.google.firebase.firestore.PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @com.google.firebase.firestore.PropertyName("image_url")
    public void setImage_url(String imageUrl) {
        if (this.imageUrl == null || this.imageUrl.trim().isEmpty()) {
            this.imageUrl = imageUrl;
        }
    }

    @com.google.firebase.firestore.PropertyName("image")
    public void setImage(String imageUrl) {
        if (this.imageUrl == null || this.imageUrl.trim().isEmpty()) {
            this.imageUrl = imageUrl;
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double getRating() {
        return rating <= 0 ? 4.5 : rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPrepTime() {
        return prepTime == null || prepTime.isEmpty() ? "10-15 mins" : prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isVeg() {
        return isVeg;
    }

    public void setVeg(boolean veg) {
        isVeg = veg;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }
}
