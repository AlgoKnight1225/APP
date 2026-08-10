package com.canteen.foodordering.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.CartAdapter;
import com.canteen.foodordering.databinding.ActivityCartBinding;
import com.canteen.foodordering.viewmodels.CartViewModel;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemChangeListener {
    private ActivityCartBinding binding;
    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        cartAdapter = new CartAdapter(this);
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(cartAdapter);

        observeCart();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnProceedCheckout.setOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
        });
    }

    private void observeCart() {
        cartViewModel.getCartLiveData().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                cartAdapter.setCartItems(items);
                binding.layoutEmptyCart.setVisibility(View.GONE);
                binding.cardCheckoutSummary.setVisibility(View.VISIBLE);
                updateBillSummary();
            } else {
                cartAdapter.setCartItems(new ArrayList<>());
                binding.layoutEmptyCart.setVisibility(View.VISIBLE);
                binding.cardCheckoutSummary.setVisibility(View.GONE);
            }
        });
    }

    private void updateBillSummary() {
        binding.tvSubtotal.setText(String.format("₹%.2f", cartViewModel.getSubtotal()));
        binding.tvTax.setText(String.format("₹%.2f", cartViewModel.getTax()));
        binding.tvTotalAmount.setText(String.format("₹%.2f", cartViewModel.getTotalAmount()));
    }

    @Override
    public void onQuantityChanged(String foodId, int newQuantity) {
        cartViewModel.updateQuantity(foodId, newQuantity);
    }

    @Override
    public void onItemRemoved(String foodId) {
        cartViewModel.removeItem(foodId);
    }
}
