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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentOrderAdapter extends RecyclerView.Adapter<StudentOrderAdapter.ViewHolder> {

    private final Context context;
    private final List<Order> orderList;

    public StudentOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText(String.format("Order #%s", order.getOrderId() != null && order.getOrderId().length() > 6 ?
                order.getOrderId().substring(0, 6).toUpperCase() : order.getOrderId()));

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(order.getTimestamp())));

        holder.tvStatus.setText(order.getStatus());
        setStatusBadgeColor(holder.tvStatus, order.getStatus());

        holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "₹ %.2f", order.getTotalPrice()));

        if (order.getNotes() != null && !order.getNotes().trim().isEmpty()) {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText(String.format("Notes: %s", order.getNotes()));
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // Sub-adapter for items
        OrderItemAdapter subAdapter = new OrderItemAdapter(context, order.getItems());
        holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(context));
        holder.rvOrderItems.setAdapter(subAdapter);
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
        TextView tvOrderId, tvStatus, tvDate, tvTotalPrice, tvNotes;
        RecyclerView rvOrderItems;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
        }
    }
}
