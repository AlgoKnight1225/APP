package com.canteen.foodordering.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.repositories.CartRepository;
import com.canteen.foodordering.utils.CartManager;

import java.util.List;

public class CartViewModel extends AndroidViewModel {

    // ── In-memory CartManager (kept for legacy subtotal/tax helpers) ──────────
    private final CartManager cartManager;

    // ── Firebase-backed CartRepository ────────────────────────────────────────
    private final CartRepository cartRepository;

    public CartViewModel(@NonNull Application application) {
        super(application);
        cartManager = CartManager.getInstance();
        cartRepository = new CartRepository();
    }

    // ── Firebase pending-cart LiveData ────────────────────────────────────────

    /** Returns real-time list of pending cart items from Firestore. */
    public LiveData<List<CartItem>> getPendingCartLiveData() {
        return cartRepository.getPendingCartLiveData();
    }

    public LiveData<String> getCartMessageLiveData() {
        return cartRepository.getCartMessageLiveData();
    }

    /** Start listening to the user's pendingCart collection. */
    public void listenToPendingCart() {
        cartRepository.listenToPendingCart();
    }

    /** Save a new item to Firebase as its own unique document. */
    public void addPendingCartItem(CartItem cartItem) {
        cartRepository.addPendingCartItem(cartItem);
    }

    /** Remove ONLY one specific item from Firebase pendingCart. */
    public void removePendingCartItem(String cartItemId) {
        cartRepository.removePendingCartItem(cartItemId);
    }

    /** Update quantity of one specific pending cart item. */
    public void updatePendingCartItemQuantity(String cartItemId, int newQuantity) {
        cartRepository.updatePendingCartItemQuantity(cartItemId, newQuantity);
    }

    // ── Legacy in-memory CartManager (used by CheckoutActivity subtotal math) ─

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

    public void updateQuantity(String cartItemId, int quantity) {
        cartManager.updateQuantity(cartItemId, quantity);
    }

    public void removeItem(String cartItemId) {
        cartManager.removeItem(cartItemId);
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

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCleared() {
        super.onCleared();
        cartRepository.detachListener();
    }
}
