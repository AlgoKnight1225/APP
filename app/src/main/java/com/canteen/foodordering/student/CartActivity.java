package com.canteen.foodordering.student;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.CartAdapter;
import com.canteen.foodordering.databinding.ActivityCartBinding;
import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.models.User;
import com.canteen.foodordering.utils.CartManager;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private CartAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String studentName = "Student";
    private String studentPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupCartList();
        fetchStudentDetails();

        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void setupCartList() {
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        adapter = new CartAdapter(this, cartItems, this::updateUI);

        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(adapter);

        updateUI();
    }

    private void updateUI() {
        List<CartItem> items = CartManager.getInstance().getCartItems();
        if (items.isEmpty()) {
            binding.tvEmptyCart.setVisibility(View.VISIBLE);
            binding.rvCartItems.setVisibility(View.GONE);
            binding.cardBottomCheckout.setVisibility(View.GONE);
        } else {
            binding.tvEmptyCart.setVisibility(View.GONE);
            binding.rvCartItems.setVisibility(View.VISIBLE);
            binding.cardBottomCheckout.setVisibility(View.VISIBLE);
            binding.tvTotalAmount.setText(String.format(Locale.getDefault(), "₹ %.2f", CartManager.getInstance().getTotalAmount()));
        }
    }

    private void fetchStudentDetails() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection(Constants.COLLECTION_USERS)
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User u = documentSnapshot.toObject(User.class);
                            if (u != null) {
                                studentName = u.getName();
                                studentPhone = u.getPhone();
                            }
                        }
                    });
        }
    }

    private void placeOrder() {
        List<CartItem> items = new ArrayList<>(CartManager.getInstance().getCartItems());
        if (items.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        String orderId = db.collection(Constants.COLLECTION_ORDERS).document().getId();
        String notes = binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : "";
        double total = CartManager.getInstance().getTotalAmount();

        Order order = new Order(
                orderId,
                currentUser.getUid(),
                studentName,
                studentPhone,
                items,
                total,
                Constants.STATUS_PLACED,
                System.currentTimeMillis(),
                notes
        );

        db.collection(Constants.COLLECTION_ORDERS)
                .document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    CartManager.getInstance().clearCart();
                    Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(CartActivity.this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnPlaceOrder.setEnabled(!isLoading);
    }
}
