package com.canteen.foodordering.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.AdminOrderAdapter;
import com.canteen.foodordering.databinding.FragmentAdminOrdersBinding;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.viewmodels.OrderViewModel;

import java.util.ArrayList;

public class AdminOrdersFragment extends Fragment implements AdminOrderAdapter.OnOrderStatusChangeListener {
    private FragmentAdminOrdersBinding binding;
    private OrderViewModel orderViewModel;
    private AdminOrderAdapter orderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);

        orderAdapter = new AdminOrderAdapter(this);
        binding.rvAdminOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminOrders.setAdapter(orderAdapter);

        observeOrders();
    }

    private void observeOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);

        orderViewModel.getAdminOrdersLiveData().observe(getViewLifecycleOwner(), orders -> {
            binding.progressBar.setVisibility(View.GONE);
            if (orders != null && !orders.isEmpty()) {
                orderAdapter.setOrderList(orders);
                binding.layoutEmpty.setVisibility(View.GONE);
            } else {
                orderAdapter.setOrderList(new ArrayList<>());
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            }
        });

        orderViewModel.listenToAllAdminOrders();
    }

    @Override
    public void onStatusChanged(Order order, String newStatus) {
        orderViewModel.updateOrderStatus(order.getOrderId(), newStatus);
        Toast.makeText(requireContext(), "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
