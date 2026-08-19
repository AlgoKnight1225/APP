package com.canteen.foodordering.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.canteen.foodordering.databinding.ActivityCheckoutBinding;
import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.models.User;
import com.canteen.foodordering.viewmodels.AuthViewModel;
import com.canteen.foodordering.viewmodels.CartViewModel;
import com.canteen.foodordering.viewmodels.OrderViewModel;

import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private ActivityCheckoutBinding binding;
    private AuthViewModel authViewModel;
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;

    private User currentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        observeData();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnConfirmOrder.setOnClickListener(v -> placeOrder());
    }

    private void observeData() {
        authViewModel.getUserProfileLiveData().observe(this, user -> {
            if (user != null) {
                currentUserProfile = user;
                binding.tvCustomerName.setText("Name: " + (user.getName() != null ? user.getName() : "Student"));
                binding.tvCustomerPhone.setText("Phone: " + (user.getPhone() != null ? user.getPhone() : "N/A"));
            }
        });

        if (authViewModel.getCurrentUser() != null) {
            authViewModel.fetchUserProfile(authViewModel.getCurrentUser().getUid());
        }

        binding.tvCheckoutTotal.setText(String.format("₹%.2f", cartViewModel.getTotalAmount()));

        orderViewModel.getOrderStatusMessageLiveData().observe(this, message -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnConfirmOrder.setEnabled(true);
            if ("ORDER_PLACED_SUCCESS".equals(message)) {
                Toast.makeText(this, "Order Placed Successfully! 🎉", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(CheckoutActivity.this, StudentMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void placeOrder() {
        List<CartItem> cartItems = cartViewModel.getCartLiveData().getValue();
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = binding.etPickupNotes.getText().toString().trim();
        String paymentMethod = binding.rbUPI.isChecked() ? "UPI / Online" : "Cash at Counter";

        String studentId = authViewModel.getCurrentUser() != null ? authViewModel.getCurrentUser().getUid() : "";
        String studentName = currentUserProfile != null ? currentUserProfile.getName() : "Student";
        String studentPhone = currentUserProfile != null ? currentUserProfile.getPhone() : "";

        Order newOrder = new Order(
                null,
                studentId,
                studentName,
                studentPhone,
                cartItems,
                cartViewModel.getTotalAmount(),
                cartViewModel.getSubtotal(),
                cartViewModel.getTax(),
                "PENDING",
                System.currentTimeMillis(),
                notes,
                paymentMethod
        );

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnConfirmOrder.setEnabled(false);
        orderViewModel.placeOrder(newOrder);
    }
}
