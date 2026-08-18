package com.canteen.foodordering.student;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ActivityOrderTrackingBinding;
import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Date;

public class OrderTrackingActivity extends AppCompatActivity {
    private ActivityOrderTrackingBinding binding;
    private FirebaseFirestore db;
    private ListenerRegistration orderListener;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra("ORDER_ID")) {
            orderId = getIntent().getStringExtra("ORDER_ID");
        } else if (getIntent().hasExtra("ORDER")) {
            Order orderObj = (Order) getIntent().getSerializableExtra("ORDER");
            if (orderObj != null) {
                orderId = orderObj.getOrderId();
            }
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBackHome.setOnClickListener(v -> finish());

        if (orderId != null && !orderId.isEmpty()) {
            listenToOrderUpdates();
        } else {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void listenToOrderUpdates() {
        binding.progressBar.setVisibility(View.VISIBLE);

        orderListener = db.collection(Constants.COLLECTION_ORDERS)
                .document(orderId)
                .addSnapshotListener((snapshot, error) -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this, "Error tracking order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Order order = snapshot.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(snapshot.getId());
                            updateUI(order);
                        }
                    }
                });
    }

    private void updateUI(Order order) {
        String displayId = "#" + (order.getOrderId() != null && order.getOrderId().length() > 8 ?
                order.getOrderId().substring(0, 8).toUpperCase() : order.getOrderId());
        binding.tvOrderId.setText(displayId);

        String dateString = DateFormat.format("dd MMM yyyy, hh:mm a", new Date(order.getTimestamp())).toString();
        binding.tvOrderDate.setText(dateString);
        binding.tvTotalAmount.setText(String.format("₹%.2f", order.getTotalPrice()));
        binding.tvStatusBadge.setText(order.getStatus() != null ? order.getStatus().toUpperCase() : "PLACED");

        // Items Summary
        StringBuilder sb = new StringBuilder();
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                sb.append("• ").append(item.getQuantity()).append("x ").append(item.getFoodName()).append("\n");
            }
        }
        binding.tvOrderItemsList.setText(sb.length() > 0 ? sb.toString().trim() : "No items listed");

        // Timeline Progress Update
        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
        resetTimeline();

        if ("PENDING".equals(status) || "PLACED".equals(status)) {
            setStepState(binding.ivStepPlaced, binding.tvStepPlaced, true);
        } else if ("CONFIRMED".equals(status)) {
            setStepState(binding.ivStepPlaced, binding.tvStepPlaced, true);
            setStepState(binding.ivStepConfirmed, binding.tvStepConfirmed, true);
            binding.linePlacedToConfirmed.setBackgroundResource(R.color.primary);
        } else if ("PREPARING".equals(status)) {
            setStepState(binding.ivStepPlaced, binding.tvStepPlaced, true);
            setStepState(binding.ivStepConfirmed, binding.tvStepConfirmed, true);
            setStepState(binding.ivStepPreparing, binding.tvStepPreparing, true);
            binding.linePlacedToConfirmed.setBackgroundResource(R.color.primary);
            binding.lineConfirmedToPreparing.setBackgroundResource(R.color.primary);
        } else if ("READY".equals(status)) {
            setStepState(binding.ivStepPlaced, binding.tvStepPlaced, true);
            setStepState(binding.ivStepConfirmed, binding.tvStepConfirmed, true);
            setStepState(binding.ivStepPreparing, binding.tvStepPreparing, true);
            setStepState(binding.ivStepReady, binding.tvStepReady, true);
            binding.linePlacedToConfirmed.setBackgroundResource(R.color.primary);
            binding.lineConfirmedToPreparing.setBackgroundResource(R.color.primary);
            binding.linePreparingToReady.setBackgroundResource(R.color.primary);
        } else if ("COMPLETED".equals(status) || "DELIVERED".equals(status)) {
            setStepState(binding.ivStepPlaced, binding.tvStepPlaced, true);
            setStepState(binding.ivStepConfirmed, binding.tvStepConfirmed, true);
            setStepState(binding.ivStepPreparing, binding.tvStepPreparing, true);
            setStepState(binding.ivStepReady, binding.tvStepReady, true);
            setStepState(binding.ivStepCompleted, binding.tvStepCompleted, true);
            binding.linePlacedToConfirmed.setBackgroundResource(R.color.primary);
            binding.lineConfirmedToPreparing.setBackgroundResource(R.color.primary);
            binding.linePreparingToReady.setBackgroundResource(R.color.primary);
            binding.lineReadyToCompleted.setBackgroundResource(R.color.primary);
        }
    }

    private void resetTimeline() {
        setStepState(binding.ivStepPlaced, binding.tvStepPlaced, false);
        setStepState(binding.ivStepConfirmed, binding.tvStepConfirmed, false);
        setStepState(binding.ivStepPreparing, binding.tvStepPreparing, false);
        setStepState(binding.ivStepReady, binding.tvStepReady, false);
        setStepState(binding.ivStepCompleted, binding.tvStepCompleted, false);

        binding.linePlacedToConfirmed.setBackgroundResource(R.color.border_light);
        binding.lineConfirmedToPreparing.setBackgroundResource(R.color.border_light);
        binding.linePreparingToReady.setBackgroundResource(R.color.border_light);
        binding.lineReadyToCompleted.setBackgroundResource(R.color.border_light);
    }

    private void setStepState(android.widget.ImageView icon, android.widget.TextView label, boolean active) {
        if (active) {
            icon.setImageResource(R.drawable.ic_check_circle);
            icon.setColorFilter(getColor(R.color.primary));
            label.setTextColor(getColor(R.color.text_primary));
            label.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            icon.setImageResource(R.drawable.ic_check_circle);
            icon.setColorFilter(getColor(R.color.text_muted));
            label.setTextColor(getColor(R.color.text_muted));
            label.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}
