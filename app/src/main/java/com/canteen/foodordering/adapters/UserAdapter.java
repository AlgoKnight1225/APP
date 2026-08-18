package com.canteen.foodordering.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.canteen.foodordering.R;
import com.canteen.foodordering.databinding.ItemUserAdminBinding;
import com.canteen.foodordering.models.User;
import com.canteen.foodordering.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList = new ArrayList<>();

    public void setUserList(List<User> list) {
        this.userList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserAdminBinding binding = ItemUserAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserAdminBinding binding;

        public UserViewHolder(@NonNull ItemUserAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            binding.tvUserName.setText(user.getName() != null ? user.getName() : "User");
            String emailPhone = (user.getEmail() != null ? user.getEmail() : "") + " • " +
                    (user.getPhone() != null ? user.getPhone() : "");
            binding.tvUserEmailPhone.setText(emailPhone);
            binding.tvUserRole.setText("Role: " + (user.getRole() != null ? user.getRole().toUpperCase() : "STUDENT"));
            binding.tvUserOrdersCount.setText(user.getTotalOrders() + " Orders");

            if (user.getProfileImage() != null && !user.getProfileImage().trim().isEmpty()) {
                binding.ivUserAvatar.setImageTintList(null);
                ImageLoader.loadImage(binding.ivUserAvatar, user.getProfileImage(), R.drawable.ic_person);
            } else {
                binding.ivUserAvatar.setImageResource(R.drawable.ic_person);
            }
        }
    }
}
