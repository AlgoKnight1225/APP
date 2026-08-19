package com.canteen.foodordering.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.canteen.foodordering.BuildConfig;
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
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.List;

/**
 * CheckoutActivity
 *
 * Payment flow:
 *   - Cash at Counter  → places order directly in Firestore (unchanged existing flow)
 *   - UPI / Card / Wallet → opens Razorpay Checkout → on success, places order in Firestore
 *
 * Razorpay Key ID is read from BuildConfig (sourced from local.properties at build time).
 * The Razorpay Secret Key is NEVER present in this file or anywhere in the Android app.
 */
public class CheckoutActivity extends AppCompatActivity implements PaymentResultListener {

    private ActivityCheckoutBinding binding;
    private AuthViewModel authViewModel;
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private CouponRepository couponRepository;

    private User currentUserProfile;
    private Coupon appliedCoupon = null;
    private double appliedDiscount = 0.0;

    /**
     * Holds the Order object while Razorpay payment is in progress.
     * Set before opening Razorpay, consumed in onPaymentSuccess/onPaymentError.
     */
    private Order pendingOrder = null;

    // ─────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        couponRepository = new CouponRepository();

        // Pre-load Razorpay resources in the background for faster checkout open
        Checkout.preload(getApplicationContext());

        setupCouponSection();
        observeData();
        updatePaymentSummary();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnConfirmOrder.setOnClickListener(v -> placeOrder());
    }

    // ─────────────────────────────────────────────────────────────
    // Coupon Section (unchanged)
    // ─────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────
    // Payment Summary (unchanged)
    // ─────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────
    // Data Observation (unchanged)
    // ─────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────
    // Order Placement — branching between Cash and Razorpay flows
    // ─────────────────────────────────────────────────────────────

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
        String paymentMethod = getSelectedPaymentMethod();

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

        if (binding.rbCash.isChecked()) {
            // ── Cash at Counter: existing flow unchanged ──────────────
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnConfirmOrder.setEnabled(false);
            orderViewModel.placeOrder(newOrder);
        } else {
            // ── UPI / Card / Wallet: open Razorpay Checkout ───────────
            pendingOrder = newOrder;
            startRazorpayCheckout(finalTotal, studentName, studentPhone);
        }
    }

    /**
     * Returns the human-readable payment method string based on the selected RadioButton.
     */
    private String getSelectedPaymentMethod() {
        if (binding.rbCash.isChecked()) {
            return "Cash at Counter";
        } else if (binding.rbUPI.isChecked()) {
            return "UPI / Online (Razorpay)";
        } else if (binding.rbCard.isChecked()) {
            return "Card (Razorpay)";
        } else if (binding.rbWallet.isChecked()) {
            return "Wallet (Razorpay)";
        }
        return "Online (Razorpay)";
    }

    // ─────────────────────────────────────────────────────────────
    // Razorpay Checkout
    // ─────────────────────────────────────────────────────────────

    /**
     * Opens the Razorpay payment sheet.
     *
     * @param amountInRupees The final payable amount in INR (e.g. 149.50).
     *                       Razorpay requires the amount in paise, so we multiply by 100.
     * @param studentName    Prefilled name shown in the payment sheet.
     * @param studentPhone   Prefilled phone shown in the payment sheet.
     */
    private void startRazorpayCheckout(double amountInRupees, String studentName, String studentPhone) {
        // Show progress while setting up
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnConfirmOrder.setEnabled(false);

        Checkout checkout = new Checkout();

        // Key ID comes from BuildConfig (sourced from local.properties at build time)
        // The Secret Key is never present in this app.
        checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID);

        // Optional: set your app logo for the payment sheet
        checkout.setImage(R.mipmap.ic_launcher);

        try {
            JSONObject options = new JSONObject();

            // Amount must be in paise (1 INR = 100 paise)
            int amountInPaise = (int) Math.round(amountInRupees * 100);

            options.put("name", "First Bite Canteen");
            options.put("description", "Food Order Payment");
            options.put("currency", "INR");
            options.put("amount", amountInPaise);

            // Prefill student details for a smoother experience
            JSONObject prefill = new JSONObject();
            prefill.put("name", studentName != null ? studentName : "");
            prefill.put("contact", studentPhone != null ? studentPhone : "");
            options.put("prefill", prefill);

            // Theme color — matches the app's primary color
            JSONObject theme = new JSONObject();
            theme.put("color", "#5D4037"); // matches @color/primary (brown/caramel)
            options.put("theme", theme);

            checkout.open(this, options);

        } catch (Exception e) {
            // Hide progress and re-enable button on setup failure
            binding.progressBar.setVisibility(View.GONE);
            binding.btnConfirmOrder.setEnabled(true);
            pendingOrder = null;
            Toast.makeText(this, "Payment setup failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Razorpay PaymentResultListener callbacks
    // ─────────────────────────────────────────────────────────────

    /**
     * Called by Razorpay SDK when the payment is completed successfully.
     *
     * @param razorpayPaymentId The unique payment ID from Razorpay (e.g. "pay_XXXXXXXXXX").
     *                          Store this with the order for reference/reconciliation.
     */
    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        if (pendingOrder == null) {
            // Safety guard — should not happen, but handle gracefully
            Toast.makeText(this, "Payment received. Processing your order...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append Razorpay payment ID to the payment method for tracking
        String paymentMethodWithId = pendingOrder.getPaymentMethod()
                + " | ID: " + razorpayPaymentId;
        pendingOrder.setPaymentMethod(paymentMethodWithId);

        // Now place the order in Firestore — this is the exact same existing flow
        // used by Cash at Counter. Cart will be cleared and user navigated to home on success.
        orderViewModel.placeOrder(pendingOrder);
        pendingOrder = null;

        // Note: progressBar stays visible; it is hidden by the orderViewModel observer
        // (same observer used for Cash flow) when ORDER_PLACED_SUCCESS arrives.
    }

    /**
     * Called by Razorpay SDK when the payment fails OR is cancelled by the user.
     *
     * Error codes (from Razorpay docs):
     *   0 = Network error
     *   1 = Invalid options
     *   2 = Payment failed (bank/UPI declined)
     *   3 = Payment cancelled by user
     *
     * @param code     Error code.
     * @param response JSON string with error details.
     */
    @Override
    public void onPaymentError(int code, String response) {
        // Re-enable the button so the student can retry
        binding.progressBar.setVisibility(View.GONE);
        binding.btnConfirmOrder.setEnabled(true);

        // Clear the pending order; the student can try again or choose Cash
        pendingOrder = null;

        String message;
        switch (code) {
            case Checkout.NETWORK_ERROR:
                message = "Payment failed: No internet connection. Please check your network and try again.";
                break;
            case Checkout.INVALID_OPTIONS:
                message = "Payment setup error. Please contact support.";
                break;
            case Checkout.PAYMENT_CANCELED:
                message = "Payment cancelled. You can retry or choose Cash at Counter.";
                break;
            default:
                // Covers bank declines, UPI failures, etc.
                message = "Payment was not completed. Please try again or choose a different payment method.";
                break;
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
