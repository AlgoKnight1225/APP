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
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PREPARING = "PREPARING";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Food Categories (Starbucks style)
    public static final String[] CATEGORIES = {"All", "Coffee & Drinks", "Meals", "Snacks & Bakery", "Breakfast", "Desserts"};
}
