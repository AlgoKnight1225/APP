package com.canteen.foodordering.utils;

import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addItem(FoodItem foodItem) {
        for (CartItem item : cartItems) {
            if (item.getFoodId().equals(foodItem.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        cartItems.add(new CartItem(foodItem.getId(), foodItem.getName(), foodItem.getPrice(), 1, foodItem.getImageUrl()));
    }

    public void updateQuantity(String foodId, int quantity) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getFoodId().equals(foodId)) {
                if (quantity <= 0) {
                    cartItems.remove(i);
                } else {
                    cartItems.get(i).setQuantity(quantity);
                }
                return;
            }
        }
    }

    public void removeItem(String foodId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getFoodId().equals(foodId)) {
                cartItems.remove(i);
                return;
            }
        }
    }

    public void clearCart() {
        cartItems.clear();
    }

    public double getTotalAmount() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }
}
