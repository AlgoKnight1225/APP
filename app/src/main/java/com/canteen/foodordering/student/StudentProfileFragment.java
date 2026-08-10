package com.canteen.foodordering.student;

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

import com.canteen.foodordering.auth.LoginActivity;
import com.canteen.foodordering.databinding.FragmentProfileBinding;
import com.canteen.foodordering.viewmodels.AuthViewModel;

public class StudentProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private String currentUserEmail = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        observeUserProfile();

        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
        binding.btnChangePassword.setOnClickListener(v -> handleChangePassword());
        binding.btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void observeUserProfile() {
        authViewModel.getUserProfileLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUserEmail = user.getEmail() != null ? user.getEmail() : "";
                binding.tvProfileName.setText(user.getName() != null ? user.getName() : "Student");
                binding.tvProfileEmail.setText(currentUserEmail);
                binding.tvTotalOrdersCount.setText(String.valueOf(user.getTotalOrders()));
                binding.tvRoleTag.setText(user.getRole() != null ? user.getRole().toUpperCase() : "STUDENT");

                binding.etEditName.setText(user.getName());
                binding.etEditPhone.setText(user.getPhone());
            }
        });

        if (authViewModel.getCurrentUser() != null) {
            authViewModel.fetchUserProfile(authViewModel.getCurrentUser().getUid());
        }

        authViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), message -> {
            if ("RESET_SUCCESS".equals(message)) {
                Toast.makeText(requireContext(), "Password reset email sent to " + currentUserEmail, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProfile() {
        String name = binding.etEditName.getText().toString().trim();
        String phone = binding.etEditPhone.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etEditName.setError("Name is required");
            return;
        }

        authViewModel.updateProfile(name, phone, "");
        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void handleChangePassword() {
        if (!currentUserEmail.isEmpty()) {
            authViewModel.sendPasswordResetEmail(currentUserEmail);
        } else {
            Toast.makeText(requireContext(), "Email address unavailable", Toast.LENGTH_SHORT).show();
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
