package com.canteen.foodordering.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.StudentOrderAdapter;
import com.canteen.foodordering.databinding.FragmentStudentOrdersBinding;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentOrdersFragment extends Fragment {

    private FragmentStudentOrdersBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final List<Order> orderList = new ArrayList<>();
    private StudentOrderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();

        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchStudentOrders);
        fetchStudentOrders();
    }

    private void setupRecyclerView() {
        adapter = new StudentOrderAdapter(requireContext(), orderList);
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrders.setAdapter(adapter);
    }

    private void fetchStudentOrders() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo("studentId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;

                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);

                    if (error != null) {
                        Toast.makeText(requireContext(), "Error loading orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            orderList.add(order);
                        }
                        adapter.notifyDataSetChanged();

                        if (orderList.isEmpty()) {
                            binding.tvEmptyOrders.setVisibility(View.VISIBLE);
                            binding.rvOrders.setVisibility(View.GONE);
                        } else {
                            binding.tvEmptyOrders.setVisibility(View.GONE);
                            binding.rvOrders.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
