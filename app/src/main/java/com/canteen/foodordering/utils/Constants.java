package com.canteen.foodordering.utils;

public class Constants {
    // Firestore Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_FOOD = "food_items";
    public static final String COLLECTION_ORDERS = "orders";

    // User Roles
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ADMIN = "admin";

    // Order Statuses
    public static final String STATUS_PLACED = "Placed";
    public static final String STATUS_PREPARING = "Preparing";
    public static final String STATUS_READY = "Ready";
    public static final String STATUS_DELIVERED = "Delivered";

    // Food Categories
    public static final String[] CATEGORIES = {"All", "Breakfast", "Meals", "Snacks", "Beverages", "Desserts"};
}
