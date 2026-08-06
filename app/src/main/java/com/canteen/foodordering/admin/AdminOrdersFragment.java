package com.canteen.foodordering.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.AdminOrderAdapter;
import com.canteen.foodordering.databinding.FragmentAdminOrdersBinding;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersFragment extends Fragment {

    private FragmentAdminOrdersBinding binding;
    private FirebaseFirestore db;

    private final List<Order> allOrders = new ArrayList<>();
    private final List<Order> filteredOrders = new ArrayList<>();
    private AdminOrderAdapter adapter;
    private String selectedStatusFilter = "All";

    private final String[] statusOptions = {
            Constants.STATUS_PLACED,
            Constants.STATUS_PREPARING,
            Constants.STATUS_READY,
            Constants.STATUS_DELIVERED
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        setupStatusFilterChips();

        binding.swipeRefreshAdminOrders.setOnRefreshListener(this::fetchOrders);
        fetchOrders();
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(requireContext(), filteredOrders, this::showUpdateStatusDialog);
        binding.rvAdminOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminOrders.setAdapter(adapter);
    }

    private void setupStatusFilterChips() {
        binding.chipGroupStatus.removeAllViews();

        String[] filters = {"All", Constants.STATUS_PLACED, Constants.STATUS_PREPARING, Constants.STATUS_READY, Constants.STATUS_DELIVERED};
        for (String filter : filters) {
            Chip chip = new Chip(requireContext());
            chip.setText(filter);
            chip.setCheckable(true);
            chip.setClickable(true);
            if (filter.equalsIgnoreCase("All")) {
                chip.setChecked(true);
            }
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedStatusFilter = filter;
                    applyStatusFilter();
                }
            });
            binding.chipGroupStatus.addView(chip);
        }
    }

    private void fetchOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.COLLECTION_ORDERS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;

                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshAdminOrders.setRefreshing(false);

                    if (error != null) {
                        Toast.makeText(requireContext(), "Error fetching orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        allOrders.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            allOrders.add(order);
                        }
                        applyStatusFilter();
                    }
                });
    }

    private void applyStatusFilter() {
        filteredOrders.clear();
        for (Order order : allOrders) {
            if (selectedStatusFilter.equalsIgnoreCase("All") ||
                    (order.getStatus() != null && order.getStatus().equalsIgnoreCase(selectedStatusFilter))) {
                filteredOrders.add(order);
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredOrders.isEmpty()) {
            binding.tvEmptyAdminOrders.setVisibility(View.VISIBLE);
            binding.rvAdminOrders.setVisibility(View.GONE);
        } else {
            binding.tvEmptyAdminOrders.setVisibility(View.GONE);
            binding.rvAdminOrders.setVisibility(View.VISIBLE);
        }
    }

    private void showUpdateStatusDialog(Order order) {
        int checkedItem = 0;
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equalsIgnoreCase(order.getStatus())) {
                checkedItem = i;
                break;
            }
        }

        final int[] selectedIndex = {checkedItem};

        new AlertDialog.Builder(requireContext())
                .setTitle("Update Order Status")
                .setSingleChoiceItems(statusOptions, checkedItem, (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setPositiveButton("Update", (dialog, which) -> {
                    String newStatus = statusOptions[selectedIndex[0]];
                    updateOrderStatusInFirestore(order, newStatus);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateOrderStatusInFirestore(Order order, String newStatus) {
        if (order.getOrderId() == null) return;

        db.collection(Constants.COLLECTION_ORDERS)
                .document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
