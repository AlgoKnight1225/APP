package com.canteen.foodordering.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> cartItems;
    private final MutableLiveData<List<CartItem>> cartLiveData;
    private final MutableLiveData<Integer> itemCountLiveData;
    private final MutableLiveData<Double> totalAmountLiveData;

    private CartManager() {
        cartItems = new ArrayList<>();
        cartLiveData = new MutableLiveData<>(new ArrayList<>(cartItems));
        itemCountLiveData = new MutableLiveData<>(0);
        totalAmountLiveData = new MutableLiveData<>(0.0);
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public LiveData<List<CartItem>> getCartLiveData() {
        return cartLiveData;
    }

    public LiveData<Integer> getItemCountLiveData() {
        return itemCountLiveData;
    }

    public LiveData<Double> getTotalAmountLiveData() {
        return totalAmountLiveData;
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public void addItem(FoodItem foodItem) {
        if (foodItem == null || foodItem.getId() == null) return;
        
        boolean exists = false;
        for (CartItem item : cartItems) {
            if (item.getFoodId().equals(foodItem.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                exists = true;
                break;
            }
        }
        if (!exists) {
            cartItems.add(new CartItem(foodItem.getId(), foodItem.getName(), foodItem.getPrice(), 1, foodItem.getImageUrl()));
        }
        notifyObservers();
    }

    public void addCartItem(CartItem cartItem) {
        if (cartItem == null || cartItem.getFoodId() == null) return;
        boolean exists = false;
        for (CartItem item : cartItems) {
            if (item.getFoodId().equals(cartItem.getFoodId()) && item.getFoodName().equals(cartItem.getFoodName())) {
                item.setQuantity(item.getQuantity() + cartItem.getQuantity());
                exists = true;
                break;
            }
        }
        if (!exists) {
            cartItems.add(cartItem);
        }
        notifyObservers();
    }

    public void updateQuantity(String foodId, int quantity) {
        if (foodId == null) return;
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getFoodId().equals(foodId)) {
                if (quantity <= 0) {
                    cartItems.remove(i);
                } else {
                    cartItems.get(i).setQuantity(quantity);
                }
                break;
            }
        }
        notifyObservers();
    }

    public void removeItem(String foodId) {
        if (foodId == null) return;
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getFoodId().equals(foodId)) {
                cartItems.remove(i);
                break;
            }
        }
        notifyObservers();
    }

    public void clearCart() {
        cartItems.clear();
        notifyObservers();
    }

    public double getSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        return subtotal;
    }

    public double getTax() {
        return Math.round(getSubtotal() * 0.05 * 100.0) / 100.0; // 5% GST tax
    }

    public double getTotalAmount() {
        return Math.round((getSubtotal() + getTax()) * 100.0) / 100.0;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    private void notifyObservers() {
        cartLiveData.setValue(new ArrayList<>(cartItems));
        itemCountLiveData.setValue(getItemCount());
        totalAmountLiveData.setValue(getTotalAmount());
    }
}
