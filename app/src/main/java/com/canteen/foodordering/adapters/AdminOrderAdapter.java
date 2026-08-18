package com.canteen.foodordering.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ItemOrderAdminBinding;
import com.canteen.foodordering.models.CartItem;
import com.canteen.foodordering.models.Order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {
    private List<Order> orderList = new ArrayList<>();
    private final OnOrderStatusChangeListener listener;
    private final String[] statusOptions = {"PENDING", "PREPARING", "READY", "COMPLETED", "CANCELLED"};

    public interface OnOrderStatusChangeListener {
        void onStatusChanged(Order order, String newStatus);
    }

    public AdminOrderAdapter(OnOrderStatusChangeListener listener) {
        this.listener = listener;
    }

    public void setOrderList(List<Order> list) {
        this.orderList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderAdminBinding binding = ItemOrderAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order, statusOptions, listener);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderAdminBinding binding;

        public AdminOrderViewHolder(@NonNull ItemOrderAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order, String[] statusOptions, OnOrderStatusChangeListener listener) {
            String orderIdText = "Order #" + (order.getOrderId() != null && order.getOrderId().length() > 6 ?
                    order.getOrderId().substring(0, 6).toUpperCase() : order.getOrderId());
            binding.tvAdminOrderId.setText(orderIdText);

            String customerText = "Customer: " + (order.getStudentName() != null ? order.getStudentName() : "Student")
                    + " (" + (order.getStudentPhone() != null ? order.getStudentPhone() : "N/A") + ")";
            binding.tvCustomerNamePhone.setText(customerText);

            String dateString = DateFormat.format("dd MMM yyyy, hh:mm a", new Date(order.getTimestamp())).toString();
            binding.tvAdminOrderDate.setText("Placed on: " + dateString);
            binding.tvAdminOrderTotal.setText(String.format("₹%.2f", order.getTotalPrice()));

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
            binding.tvAdminOrderItems.setText(sb.length() > 0 ? sb.toString() : "No items");

            // Setup status spinner
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(binding.getRoot().getContext(),
                    android.R.layout.simple_spinner_item, statusOptions);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerStatus.setAdapter(spinnerAdapter);

            int currentStatusIndex = 0;
            String currentStatus = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
            for (int i = 0; i < statusOptions.length; i++) {
                if (statusOptions[i].equalsIgnoreCase(currentStatus)) {
                    currentStatusIndex = i;
                    break;
                }
            }
            binding.spinnerStatus.setSelection(currentStatusIndex, false);

            final int initialIndex = currentStatusIndex;
            binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != initialIndex && listener != null) {
                        listener.onStatusChanged(order, statusOptions[position]);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }
}
