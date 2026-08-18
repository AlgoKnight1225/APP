package com.canteen.foodordering.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ItemOrderStudentBinding;
import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.student.OrderTrackingActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentOrderAdapter extends RecyclerView.Adapter<StudentOrderAdapter.OrderViewHolder> {
    private List<Order> orderList = new ArrayList<>();

    public void setOrderList(List<Order> list) {
        this.orderList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderStudentBinding binding = ItemOrderStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderStudentBinding binding;

        public OrderViewHolder(@NonNull ItemOrderStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            String orderIdText = "Order #" + (order.getOrderId() != null && order.getOrderId().length() > 6 ?
                    order.getOrderId().substring(0, 6).toUpperCase() : order.getOrderId());
            binding.tvOrderId.setText(orderIdText);

            String dateString = DateFormat.format("dd MMM yyyy, hh:mm a", new Date(order.getTimestamp())).toString();
            binding.tvOrderDate.setText(dateString);

            String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
            binding.tvOrderStatus.setText(status);

            int primaryColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.primary);
            int mutedColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.text_muted);

            // Timeline steps highlight
            resetSteps(mutedColor);

            if ("PENDING".equals(status)) {
                highlightStep(binding.stepPlaced, primaryColor);
            } else if ("PREPARING".equals(status)) {
                highlightStep(binding.stepPlaced, primaryColor);
                highlightStep(binding.stepPreparing, primaryColor);
            } else if ("READY".equals(status)) {
                highlightStep(binding.stepPlaced, primaryColor);
                highlightStep(binding.stepPreparing, primaryColor);
                highlightStep(binding.stepReady, primaryColor);
            } else if ("COMPLETED".equals(status)) {
                highlightStep(binding.stepPlaced, primaryColor);
                highlightStep(binding.stepPreparing, primaryColor);
                highlightStep(binding.stepReady, primaryColor);
                highlightStep(binding.stepCompleted, primaryColor);
            }

            StringBuilder sb = new StringBuilder();
            if (order.getItems() != null) {
                for (int i = 0; i < order.getItems().size(); i++) {
                    CartItem item = order.getItems().get(i);
                    sb.append(item.getQuantity()).append("x ").append(item.getFoodName());
                    if (i < order.getItems().size() - 1) {
                        sb.append(", ");
                    }
                }
            }
            binding.tvOrderItemsSummary.setText(sb.length() > 0 ? sb.toString() : "No items");

            binding.tvPaymentMethod.setText("Payment: " + order.getPaymentMethod());
            binding.tvOrderTotal.setText(String.format("₹%.2f", order.getTotalPrice()));

            binding.getRoot().setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(binding.getRoot().getContext(), OrderTrackingActivity.class);
                intent.putExtra("ORDER_ID", order.getOrderId());
                intent.putExtra("ORDER", order);
                binding.getRoot().getContext().startActivity(intent);
            });
        }

        private void resetSteps(int mutedColor) {
            binding.stepPlaced.setTextColor(mutedColor);
            binding.stepPreparing.setTextColor(mutedColor);
            binding.stepReady.setTextColor(mutedColor);
            binding.stepCompleted.setTextColor(mutedColor);
        }

        private void highlightStep(TextView stepView, int activeColor) {
            stepView.setTextColor(activeColor);
        }
    }
}
