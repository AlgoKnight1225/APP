package com.canteen.foodordering.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.utils.CartManager;

import java.util.List;

public class CartViewModel extends AndroidViewModel {
    private final CartManager cartManager;

    public CartViewModel(@NonNull Application application) {
        super(application);
        cartManager = CartManager.getInstance();
    }

    public LiveData<List<CartItem>> getCartLiveData() {
        return cartManager.getCartLiveData();
    }

    public LiveData<Integer> getItemCountLiveData() {
        return cartManager.getItemCountLiveData();
    }

    public LiveData<Double> getTotalAmountLiveData() {
        return cartManager.getTotalAmountLiveData();
    }

    public void addItem(FoodItem foodItem) {
        cartManager.addItem(foodItem);
    }

    public void addCartItem(CartItem cartItem) {
        cartManager.addCartItem(cartItem);
    }

    public void updateQuantity(String foodId, int quantity) {
        cartManager.updateQuantity(foodId, quantity);
    }

    public void removeItem(String foodId) {
        cartManager.removeItem(foodId);
    }

    public void clearCart() {
        cartManager.clearCart();
    }

    public double getSubtotal() {
        return cartManager.getSubtotal();
    }

    public double getTax() {
        return cartManager.getTax();
    }

    public double getTotalAmount() {
        return cartManager.getTotalAmount();
    }
}
