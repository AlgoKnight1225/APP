package com.canteen.foodordering.models;

import java.io.Serializable;
import java.util.UUID;

public class CartItem implements Serializable {
    private String cartItemId; // unique per cart row (UUID)
    private String userId;     // owner — used to filter the top-level pendingCart collection
    private String foodId;
    private String foodName;
    private double price;
    private int quantity;
    private String imageUrl;
    private long timestamp;

    public CartItem() {
        this.cartItemId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public CartItem(String foodId, String foodName, double price, int quantity, String imageUrl) {
        this.cartItemId = UUID.randomUUID().toString();
        this.foodId = foodId;
        this.foodName = foodName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}
