package com.canteen.foodordering.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private String role; // "student" or "admin"
    private long createdDate;
    private long lastLogin;
    private int totalOrders;
    private List<String> favoriteIds;

    // Required empty constructor for Firestore
    public User() {
        this.favoriteIds = new ArrayList<>();
    }

    public User(String uid, String name, String email, String phone, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profileImage = "";
        this.role = role;
        this.createdDate = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
        this.totalOrders = 0;
        this.favoriteIds = new ArrayList<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public List<String> getFavoriteIds() {
        if (favoriteIds == null) {
            favoriteIds = new ArrayList<>();
        }
        return favoriteIds;
    }

    public void setFavoriteIds(List<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
    }
}
