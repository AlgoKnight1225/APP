package com.canteen.foodordering.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ActivityCheckoutBinding;
import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.Coupon;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.models.User;
import com.canteen.foodordering.repositories.CouponRepository;
import com.canteen.foodordering.viewmodels.AuthViewModel;
import com.canteen.foodordering.viewmodels.CartViewModel;
import com.canteen.foodordering.viewmodels.OrderViewModel;
import com.google.android.material.chip.Chip;

import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private ActivityCheckoutBinding binding;
    private AuthViewModel authViewModel;
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private CouponRepository couponRepository;

    private User currentUserProfile;
    private Coupon appliedCoupon = null;
    private double appliedDiscount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        couponRepository = new CouponRepository();

        setupCouponSection();
        observeData();
        updatePaymentSummary();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnConfirmOrder.setOnClickListener(v -> placeOrder());
    }

    private void setupCouponSection() {
        binding.btnApplyCoupon.setOnClickListener(v -> applyCoupon());
        binding.btnRemoveCoupon.setOnClickListener(v -> removeCoupon());
        setupCouponSuggestions();
    }

    private void setupCouponSuggestions() {
        binding.chipGroupCoupons.removeAllViews();
        List<Coupon> suggestions = couponRepository.getSuggestedCoupons();
        for (Coupon coupon : suggestions) {
            Chip chip = new Chip(this);
            chip.setText(coupon.getCode() + " • " + coupon.getTitle());
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setChipBackgroundColorResource(R.color.bg_light_cream);
            chip.setTextColor(getColor(R.color.primary));
            chip.setChipStrokeColorResource(R.color.border_light);
            chip.setChipStrokeWidth(1f);
            chip.setOnClickListener(v -> {
                binding.etCouponCode.setText(coupon.getCode());
                applyCoupon();
            });
            binding.chipGroupCoupons.addView(chip);
        }
    }

    private void applyCoupon() {
        String code = binding.etCouponCode.getText() != null ? binding.etCouponCode.getText().toString().trim() : "";
        if (code.isEmpty()) {
            showCouponStatus("Please enter a coupon code", true);
            return;
        }

        if (appliedCoupon != null && appliedCoupon.getCode().equalsIgnoreCase(code)) {
            showCouponStatus("This coupon is already applied", true);
            return;
        }

        double subtotal = cartViewModel.getSubtotal();
        if (subtotal <= 0) {
            showCouponStatus("Your cart is empty", true);
            return;
        }

        binding.btnApplyCoupon.setEnabled(false);
        couponRepository.validateCoupon(code, subtotal, new CouponRepository.CouponValidationCallback() {
            @Override
            public void onSuccess(Coupon coupon, double discountAmount, String message) {
                binding.btnApplyCoupon.setEnabled(true);
                appliedCoupon = coupon;
                appliedDiscount = discountAmount;

                // Update UI state
                binding.layoutCouponInput.setVisibility(View.GONE);
                binding.layoutAppliedCoupon.setVisibility(View.VISIBLE);
                binding.tvAppliedCouponCode.setText(coupon.getCode() + " Applied 🎉");
                binding.tvAppliedCouponSavings.setText(String.format("You saved ₹%.2f with this coupon!", discountAmount));

                binding.tvCouponStatus.setVisibility(View.GONE);
                binding.tvCouponSuggestionsLabel.setVisibility(View.GONE);
                binding.scrollCouponSuggestions.setVisibility(View.GONE);

                updatePaymentSummary();
                Toast.makeText(CheckoutActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                binding.btnApplyCoupon.setEnabled(true);
                showCouponStatus(errorMessage, true);
                Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeCoupon() {
        appliedCoupon = null;
        appliedDiscount = 0.0;

        binding.layoutAppliedCoupon.setVisibility(View.GONE);
        binding.layoutCouponInput.setVisibility(View.VISIBLE);
        binding.etCouponCode.setText("");
        binding.tvCouponStatus.setVisibility(View.GONE);
        binding.tvCouponSuggestionsLabel.setVisibility(View.VISIBLE);
        binding.scrollCouponSuggestions.setVisibility(View.VISIBLE);

        updatePaymentSummary();
        Toast.makeText(this, "Coupon removed", Toast.LENGTH_SHORT).show();
    }

    private void showCouponStatus(String message, boolean isError) {
        binding.tvCouponStatus.setText(message);
        binding.tvCouponStatus.setTextColor(getColor(isError ? R.color.red_error : R.color.success));
        binding.tvCouponStatus.setVisibility(View.VISIBLE);
    }

    private void updatePaymentSummary() {
        double subtotal = cartViewModel.getSubtotal();
        double tax = cartViewModel.getTax();

        if (appliedCoupon != null) {
            if (subtotal < appliedCoupon.getMinOrderAmount()) {
                // Min order not met anymore
                removeCoupon();
                Toast.makeText(this, "Coupon removed: Minimum order amount not met", Toast.LENGTH_SHORT).show();
                return;
            }
            appliedDiscount = appliedCoupon.calculateDiscount(subtotal);
            binding.rowDiscount.setVisibility(View.VISIBLE);
            binding.tvDiscountLabel.setText(String.format("Coupon Discount (%s)", appliedCoupon.getCode()));
            binding.tvSummaryDiscount.setText(String.format("- ₹%.2f", appliedDiscount));
            binding.tvAppliedCouponSavings.setText(String.format("You saved ₹%.2f with this coupon!", appliedDiscount));
        } else {
            appliedDiscount = 0.0;
            binding.rowDiscount.setVisibility(View.GONE);
        }

        double finalPayable = Math.max(0.0, Math.round((subtotal + tax - appliedDiscount) * 100.0) / 100.0);

        binding.tvSummarySubtotal.setText(String.format("₹%.2f", subtotal));
        binding.tvSummaryTax.setText(String.format("₹%.2f", tax));
        binding.tvSummaryGrandTotal.setText(String.format("₹%.2f", finalPayable));
        binding.tvCheckoutTotal.setText(String.format("₹%.2f", finalPayable));
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

        cartViewModel.getCartLiveData().observe(this, items -> {
            updatePaymentSummary();
        });

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

        double subtotal = cartViewModel.getSubtotal();
        double tax = cartViewModel.getTax();
        double finalTotal = Math.max(0.0, Math.round((subtotal + tax - appliedDiscount) * 100.0) / 100.0);

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
                finalTotal,
                subtotal,
                tax,
                "PENDING",
                System.currentTimeMillis(),
                notes,
                paymentMethod
        );
        newOrder.setCouponCode(appliedCoupon != null ? appliedCoupon.getCode() : "");
        newOrder.setDiscount(appliedDiscount);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnConfirmOrder.setEnabled(false);
        orderViewModel.placeOrder(newOrder);
    }
}

