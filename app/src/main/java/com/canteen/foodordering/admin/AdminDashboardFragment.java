package com.canteen.foodordering.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.canteen.foodordering.R;
import com.canteen.foodordering.auth.LoginActivity;
import com.canteen.foodordering.databinding.FragmentAdminDashboardBinding;
import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.models.User;
import com.canteen.foodordering.viewmodels.AuthViewModel;
import com.canteen.foodordering.viewmodels.FoodViewModel;
import com.canteen.foodordering.viewmodels.OrderViewModel;

public class AdminDashboardFragment extends Fragment {
    private FragmentAdminDashboardBinding binding;
    private OrderViewModel orderViewModel;
    private FoodViewModel foodViewModel;
    private AuthViewModel authViewModel;
    private User currentAdminUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        foodViewModel = new ViewModelProvider(requireActivity()).get(FoodViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        observeDashboardMetrics();

        binding.btnSaveAdminPhoto.setOnClickListener(v -> saveAdminProfilePhotoUrl());
        binding.btnAdminLogout.setOnClickListener(v -> handleLogout());
    }

    private void observeDashboardMetrics() {
        authViewModel.getUserProfileLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentAdminUser = user;
                binding.tvAdminName.setText(user.getName() != null ? user.getName() : "Admin Manager");
                binding.tvAdminEmail.setText(user.getEmail() != null ? user.getEmail() : "");

                String currentImage = user.getProfileImage() != null ? user.getProfileImage() : "";
                if (binding.etAdminProfileImageUrl.getText() == null || binding.etAdminProfileImageUrl.getText().toString().isEmpty()) {
                    binding.etAdminProfileImageUrl.setText(currentImage);
                }

                if (!currentImage.trim().isEmpty() && isAdded()) {
                    binding.ivAdminProfilePic.setImageTintList(null);
                    Glide.with(requireContext())
                            .load(currentImage.trim())
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(binding.ivAdminProfilePic);
                } else {
                    binding.ivAdminProfilePic.setImageResource(R.drawable.ic_person);
                }
            }
        });

        if (authViewModel.getCurrentUser() != null) {
            authViewModel.fetchUserProfile(authViewModel.getCurrentUser().getUid());
        }

        orderViewModel.getAdminOrdersLiveData().observe(getViewLifecycleOwner(), orders -> {
            if (orders != null) {
                double totalRevenue = 0.0;
                int activeOrdersCount = 0;

                for (Order order : orders) {
                    if ("COMPLETED".equalsIgnoreCase(order.getStatus())) {
                        totalRevenue += order.getTotalPrice();
                    }
                    if ("PENDING".equalsIgnoreCase(order.getStatus()) ||
                        "PREPARING".equalsIgnoreCase(order.getStatus()) ||
                        "READY".equalsIgnoreCase(order.getStatus())) {
                        activeOrdersCount++;
                    }
                }

                binding.tvTotalRevenue.setText(String.format("₹%.2f", totalRevenue));
                binding.tvTotalOrders.setText(String.valueOf(orders.size()));
                binding.tvActiveOrders.setText(String.valueOf(activeOrdersCount));
            }
        });
        orderViewModel.listenToAllAdminOrders();

        foodViewModel.getFoodItemsLiveData().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                binding.tvMenuItemsCount.setText(String.valueOf(items.size()));
            }
        });
    }

    private void saveAdminProfilePhotoUrl() {
        if (authViewModel.getCurrentUser() == null) return;

        String photoUrl = binding.etAdminProfileImageUrl.getText() != null ? binding.etAdminProfileImageUrl.getText().toString().trim() : "";
        String name = currentAdminUser != null && currentAdminUser.getName() != null ? currentAdminUser.getName() : "Admin Manager";
        String phone = currentAdminUser != null && currentAdminUser.getPhone() != null ? currentAdminUser.getPhone() : "";

        authViewModel.updateProfile(name, phone, photoUrl);
        Toast.makeText(requireContext(), "Profile photo URL saved successfully!", Toast.LENGTH_SHORT).show();

        if (!photoUrl.isEmpty() && isAdded()) {
            binding.ivAdminProfilePic.setImageTintList(null);
            Glide.with(requireContext())
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.ivAdminProfilePic);
        } else {
            binding.ivAdminProfilePic.setImageResource(R.drawable.ic_person);
        }
    }

    private void handleLogout() {
        authViewModel.logout();
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
