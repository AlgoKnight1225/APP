package com.canteen.foodordering.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.StudentOrderAdapter;
import com.canteen.foodordering.databinding.FragmentStudentOrdersBinding;
import com.canteen.foodordering.viewmodels.OrderViewModel;

import java.util.ArrayList;

public class StudentOrdersFragment extends Fragment {
    private FragmentStudentOrdersBinding binding;
    private OrderViewModel orderViewModel;
    private StudentOrderAdapter orderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);

        orderAdapter = new StudentOrderAdapter();
        binding.rvStudentOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStudentOrders.setAdapter(orderAdapter);

        observeOrders();
    }

    private void observeOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);

        orderViewModel.getStudentOrdersLiveData().observe(getViewLifecycleOwner(), orders -> {
            binding.progressBar.setVisibility(View.GONE);
            if (orders != null && !orders.isEmpty()) {
                orderAdapter.setOrderList(orders);
                binding.layoutEmpty.setVisibility(View.GONE);
            } else {
                orderAdapter.setOrderList(new ArrayList<>());
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            }
        });

        orderViewModel.listenToStudentOrders();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
