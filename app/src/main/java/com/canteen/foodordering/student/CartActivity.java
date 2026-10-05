package com.canteen.foodordering.student;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.CartAdapter;
import com.canteen.foodordering.databinding.ActivityCartBinding;
import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.models.User;
import com.canteen.foodordering.viewmodels.AuthViewModel;
import com.canteen.foodordering.viewmodels.CartViewModel;
import com.canteen.foodordering.viewmodels.OrderViewModel;

import java.util.ArrayList;
import java.util.Collections;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemChangeListener {

    private ActivityCartBinding binding;
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private AuthViewModel authViewModel;
    private CartAdapter cartAdapter;

    private User currentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartViewModel  = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        authViewModel  = new ViewModelProvider(this).get(AuthViewModel.class);

        // Set up RecyclerView
        cartAdapter = new CartAdapter(this);
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(cartAdapter);

        // Fetch user profile (needed when placing an order)
        if (authViewModel.getCurrentUser() != null) {
            authViewModel.fetchUserProfile(authViewModel.getCurrentUser().getUid());
        }
        authViewModel.getUserProfileLiveData().observe(this, user -> {
            if (user != null) {
                currentUserProfile = user;
            }
        });

        // Start listening to Firebase pendingCart
        cartViewModel.listenToPendingCart();
        observePendingCart();

        // Observe order result messages
        observeOrderStatus();

        binding.btnBack.setOnClickListener(v -> finish());

        // "Proceed to Checkout" hidden — not needed in the new per-item order model.
        // Kept visible so the bottom card layout still shows totals; button text updated.
        binding.btnProceedCheckout.setVisibility(View.GONE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observe Firebase pendingCart
    // ─────────────────────────────────────────────────────────────────────────

    private void observePendingCart() {
        cartViewModel.getPendingCartLiveData().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                cartAdapter.setCartItems(items);
                binding.layoutEmptyCart.setVisibility(View.GONE);
                binding.cardCheckoutSummary.setVisibility(View.VISIBLE);
                updateBillSummary(items);
            } else {
                cartAdapter.setCartItems(new ArrayList<>());
                binding.layoutEmptyCart.setVisibility(View.VISIBLE);
                binding.cardCheckoutSummary.setVisibility(View.GONE);
            }
        });
    }

    private void updateBillSummary(java.util.List<CartItem> items) {
        double subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getTotalPrice();
        }
        double tax   = Math.round(subtotal * 0.05 * 100.0) / 100.0;
        double total = Math.round((subtotal + tax) * 100.0) / 100.0;

        binding.tvSubtotal.setText(String.format("₹%.2f", subtotal));
        binding.tvTax.setText(String.format("₹%.2f", tax));
        binding.tvTotalAmount.setText(String.format("₹%.2f", total));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observe order placement result
    // ─────────────────────────────────────────────────────────────────────────

    private void observeOrderStatus() {
        orderViewModel.getOrderStatusMessageLiveData().observe(this, message -> {
            if ("ORDER_PLACED_SUCCESS".equals(message)) {
                Toast.makeText(this, "Order placed successfully! 🎉", Toast.LENGTH_SHORT).show();
            } else if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CartAdapter.OnCartItemChangeListener callbacks
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onQuantityChanged(String cartItemId, int newQuantity) {
        // Update quantity of THIS item only in Firebase
        cartViewModel.updatePendingCartItemQuantity(cartItemId, newQuantity);
    }

    @Override
    public void onItemRemoved(String cartItemId) {
        // Remove THIS item only from Firebase pendingCart
        cartViewModel.removePendingCartItem(cartItemId);
    }

    @Override
    public void onOrderItem(CartItem cartItem) {
        // Show confirmation dialog before ordering this single item
        String itemName = cartItem.getFoodName();
        double itemTotal = cartItem.getTotalPrice();

        new AlertDialog.Builder(this)
                .setTitle("Place Order")
                .setMessage("Order \"" + itemName + "\" for ₹" + String.format("%.2f", itemTotal) + "?")
                .setPositiveButton("ORDER", (dialog, which) -> placeOrderForItem(cartItem))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Place order for a SINGLE item only
    // ─────────────────────────────────────────────────────────────────────────

    private void placeOrderForItem(CartItem cartItem) {
        String studentId    = authViewModel.getCurrentUser() != null
                ? authViewModel.getCurrentUser().getUid() : "";
        String studentName  = currentUserProfile != null ? currentUserProfile.getName()  : "Student";
        String studentPhone = currentUserProfile != null ? currentUserProfile.getPhone() : "";

        double subtotal = cartItem.getTotalPrice();
        double tax      = Math.round(subtotal * 0.05 * 100.0) / 100.0;
        double total    = Math.round((subtotal + tax) * 100.0) / 100.0;

        // Build an Order containing ONLY this single cart item
        Order singleItemOrder = new Order(
                null,
                studentId,
                studentName,
                studentPhone,
                Collections.singletonList(cartItem),
                total,
                subtotal,
                tax,
                "PENDING",
                System.currentTimeMillis(),
                "",
                "Cash at Counter"
        );

        // Save the order to Firestore orders collection
        orderViewModel.placeOrder(singleItemOrder);

        // Remove ONLY this item from Firebase pendingCart
        // (OrderRepository.placeOrder calls CartManager.clearCart() internally —
        //  but we override that here; we only delete the one document.)
        cartViewModel.removePendingCartItem(cartItem.getCartItemId());
    }
}
