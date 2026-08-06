package com.canteen.foodordering.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.canteen.foodordering.R;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.utils.Constants;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnOrderStatusUpdateListener listener;

    public interface OnOrderStatusUpdateListener {
        void onUpdateStatusClicked(Order order);
    }

    public AdminOrderAdapter(Context context, List<Order> orderList, OnOrderStatusUpdateListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvAdminStudentName.setText(String.format("Student: %s", order.getStudentName() != null ? order.getStudentName() : "Unknown"));
        holder.tvAdminStudentPhone.setText(String.format("Phone: %s", order.getStudentPhone() != null ? order.getStudentPhone() : "N/A"));

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
        String shortId = order.getOrderId() != null && order.getOrderId().length() > 6 ?
                order.getOrderId().substring(0, 6).toUpperCase() : order.getOrderId();
        holder.tvAdminOrderId.setText(String.format("Order #%s • %s", shortId, sdf.format(new Date(order.getTimestamp()))));

        holder.tvAdminStatusBadge.setText(order.getStatus());
        setStatusBadgeColor(holder.tvAdminStatusBadge, order.getStatus());

        holder.tvAdminTotalPrice.setText(String.format(Locale.getDefault(), "₹ %.2f", order.getTotalPrice()));

        if (order.getNotes() != null && !order.getNotes().trim().isEmpty()) {
            holder.tvAdminNotes.setVisibility(View.VISIBLE);
            holder.tvAdminNotes.setText(String.format("Notes: %s", order.getNotes()));
        } else {
            holder.tvAdminNotes.setVisibility(View.GONE);
        }

        // Sub-adapter for items
        OrderItemAdapter subAdapter = new OrderItemAdapter(context, order.getItems());
        holder.rvAdminOrderItems.setLayoutManager(new LinearLayoutManager(context));
        holder.rvAdminOrderItems.setAdapter(subAdapter);

        holder.btnUpdateStatus.setOnClickListener(v -> {
            if (listener != null) listener.onUpdateStatusClicked(order);
        });
    }

    private void setStatusBadgeColor(TextView tvStatus, String status) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(24f);

        if (Constants.STATUS_PLACED.equalsIgnoreCase(status)) {
            drawable.setColor(Color.parseColor("#2196F3")); // Blue
        } else if (Constants.STATUS_PREPARING.equalsIgnoreCase(status)) {
            drawable.setColor(Color.parseColor("#FF9800")); // Orange
        } else if (Constants.STATUS_READY.equalsIgnoreCase(status)) {
            drawable.setColor(Color.parseColor("#9C27B0")); // Purple
        } else if (Constants.STATUS_DELIVERED.equalsIgnoreCase(status)) {
            drawable.setColor(Color.parseColor("#4CAF50")); // Green
        } else {
            drawable.setColor(Color.parseColor("#757575")); // Grey
        }

        tvStatus.setBackground(drawable);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminStudentName, tvAdminStudentPhone, tvAdminOrderId, tvAdminStatusBadge, tvAdminTotalPrice, tvAdminNotes;
        RecyclerView rvAdminOrderItems;
        MaterialButton btnUpdateStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminStudentName = itemView.findViewById(R.id.tvAdminStudentName);
            tvAdminStudentPhone = itemView.findViewById(R.id.tvAdminStudentPhone);
            tvAdminOrderId = itemView.findViewById(R.id.tvAdminOrderId);
            tvAdminStatusBadge = itemView.findViewById(R.id.tvAdminStatusBadge);
            tvAdminTotalPrice = itemView.findViewById(R.id.tvAdminTotalPrice);
            tvAdminNotes = itemView.findViewById(R.id.tvAdminNotes);
            rvAdminOrderItems = itemView.findViewById(R.id.rvAdminOrderItems);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}
