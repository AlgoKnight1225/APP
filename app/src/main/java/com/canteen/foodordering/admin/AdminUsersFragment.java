package com.canteen.foodordering.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.canteen.foodordering.adapters.UserAdapter;
import com.canteen.foodordering.databinding.FragmentAdminUsersBinding;
import com.canteen.foodordering.repositories.UserRepository;

import java.util.ArrayList;

public class AdminUsersFragment extends Fragment {
    private FragmentAdminUsersBinding binding;
    private UserRepository userRepository;
    private UserAdapter userAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userRepository = new UserRepository();

        userAdapter = new UserAdapter();
        binding.rvAdminUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminUsers.setAdapter(userAdapter);

        observeUsers();
    }

    private void observeUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);

        userRepository.getUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            binding.progressBar.setVisibility(View.GONE);
            if (users != null && !users.isEmpty()) {
                userAdapter.setUserList(users);
                binding.layoutEmpty.setVisibility(View.GONE);
            } else {
                userAdapter.setUserList(new ArrayList<>());
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            }
        });

        userRepository.fetchAllUsers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
