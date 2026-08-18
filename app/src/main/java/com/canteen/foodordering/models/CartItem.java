package com.canteen.foodordering.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String foodId;
    private String foodName;
    private double price;
    private int quantity;
    private String imageUrl;

    public CartItem() {
    }

    public CartItem(String foodId, String foodName, double price, int quantity, String imageUrl) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public double getTotalPrice() {
        return price * quantity;
    }
}
