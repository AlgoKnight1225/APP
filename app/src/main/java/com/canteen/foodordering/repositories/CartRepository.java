package com.canteen.foodordering.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the per-user pending cart stored in Firestore:
 *   users/{uid}/pendingCart/{cartItemId}
 *
 * Every "Add to Cart" creates a NEW document with a unique auto-generated ID.
 * Items are never overwritten. Each item is ordered / removed independently.
 */
public class CartRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private final MutableLiveData<List<CartItem>> pendingCartLiveData;
    private final MutableLiveData<String> cartMessageLiveData;

    private ListenerRegistration cartListener;

    public CartRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        pendingCartLiveData = new MutableLiveData<>(new ArrayList<>());
        cartMessageLiveData = new MutableLiveData<>();
    }

    // ── LiveData accessors ────────────────────────────────────────────────────

    public LiveData<List<CartItem>> getPendingCartLiveData() {
        return pendingCartLiveData;
    }

    public LiveData<String> getCartMessageLiveData() {
        return cartMessageLiveData;
    }

    // ── Real-time listener ────────────────────────────────────────────────────

    /**
     * Attaches a Firestore snapshot listener so the cart list updates in real time.
     * Safe to call multiple times — detaches previous listener first.
     */
    public void listenToPendingCart() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        if (cartListener != null) {
            cartListener.remove();
        }

        cartListener = db.collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .collection(Constants.COLLECTION_PENDING_CART)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        cartMessageLiveData.setValue("Error loading cart: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<CartItem> items = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            CartItem item = doc.toObject(CartItem.class);
                            if (item != null) {
                                // Restore the cartItemId from the document ID
                                item.setCartItemId(doc.getId());
                                items.add(item);
                            }
                        }
                        // Sort oldest-first so items appear in the order they were added
                        items.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
                        pendingCartLiveData.setValue(items);
                    }
                });
    }

    // ── Write operations ──────────────────────────────────────────────────────

    /**
     * Saves a new cart item as an independent Firestore document.
     * The document ID = cartItemId (UUID), so each call creates a unique entry.
     */
    public void addPendingCartItem(CartItem cartItem) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || cartItem == null) return;

        // Use cartItemId as the Firestore document ID — guarantees uniqueness
        DocumentReference docRef = db.collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .collection(Constants.COLLECTION_PENDING_CART)
                .document(cartItem.getCartItemId());

        docRef.set(cartItem)
                .addOnSuccessListener(aVoid ->
                        cartMessageLiveData.setValue("CART_ITEM_ADDED"))
                .addOnFailureListener(e ->
                        cartMessageLiveData.setValue("Failed to save item: " + e.getMessage()));
    }

    /**
     * Removes ONLY the specific cart item by its unique cartItemId.
     * No other items are affected.
     */
    public void removePendingCartItem(String cartItemId) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || cartItemId == null) return;

        db.collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .collection(Constants.COLLECTION_PENDING_CART)
                .document(cartItemId)
                .delete()
                .addOnFailureListener(e ->
                        cartMessageLiveData.setValue("Failed to remove item: " + e.getMessage()));
    }

    /**
     * Updates the quantity of a specific cart item.
     * If quantity <= 0, the item is deleted instead.
     */
    public void updatePendingCartItemQuantity(String cartItemId, int newQuantity) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || cartItemId == null) return;

        if (newQuantity <= 0) {
            removePendingCartItem(cartItemId);
            return;
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .collection(Constants.COLLECTION_PENDING_CART)
                .document(cartItemId)
                .update("quantity", newQuantity)
                .addOnFailureListener(e ->
                        cartMessageLiveData.setValue("Failed to update quantity: " + e.getMessage()));
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    public void detachListener() {
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }
    }
}
