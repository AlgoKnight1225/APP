package com.canteen.foodordering.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.utils.CartManager;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<List<Order>> studentOrdersLiveData;
    private final MutableLiveData<List<Order>> adminOrdersLiveData;
    private final MutableLiveData<String> orderStatusMessageLiveData;
    private ListenerRegistration studentOrdersListener;
    private ListenerRegistration adminOrdersListener;

    public OrderRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        studentOrdersLiveData = new MutableLiveData<>(new ArrayList<>());
        adminOrdersLiveData = new MutableLiveData<>(new ArrayList<>());
        orderStatusMessageLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Order>> getStudentOrdersLiveData() {
        return studentOrdersLiveData;
    }

    public LiveData<List<Order>> getAdminOrdersLiveData() {
        return adminOrdersLiveData;
    }

    public LiveData<String> getOrderStatusMessageLiveData() {
        return orderStatusMessageLiveData;
    }

    public void listenToStudentOrders() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        if (studentOrdersListener != null) {
            studentOrdersListener.remove();
        }

        studentOrdersListener = db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo("studentId", currentUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        orderStatusMessageLiveData.setValue("Error loading orders: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) {
                                order.setOrderId(doc.getId());
                                orders.add(order);
                            }
                        }
                        // Sort by latest timestamp
                        orders.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                        studentOrdersLiveData.setValue(orders);
                    }
                });
    }

    public void listenToAllAdminOrders() {
        if (adminOrdersListener != null) {
            adminOrdersListener.remove();
        }

        adminOrdersListener = db.collection(Constants.COLLECTION_ORDERS)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        orderStatusMessageLiveData.setValue("Error loading admin orders: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) {
                                order.setOrderId(doc.getId());
                                orders.add(order);
                            }
                        }
                        orders.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                        adminOrdersLiveData.setValue(orders);
                    }
                });
    }

    public void placeOrder(Order order) {
        if (order == null) return;

        String docId = db.collection(Constants.COLLECTION_ORDERS).document().getId();
        order.setOrderId(docId);
        order.setTimestamp(System.currentTimeMillis());

        db.collection(Constants.COLLECTION_ORDERS).document(docId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    // Update user total orders count
                    if (order.getStudentId() != null) {
                        db.collection(Constants.COLLECTION_USERS).document(order.getStudentId())
                                .update("totalOrders", FieldValue.increment(1));
                    }
                    CartManager.getInstance().clearCart();
                    orderStatusMessageLiveData.setValue("ORDER_PLACED_SUCCESS");
                })
                .addOnFailureListener(e -> orderStatusMessageLiveData.setValue("Failed to place order: " + e.getMessage()));
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        if (orderId == null || newStatus == null) return;

        db.collection(Constants.COLLECTION_ORDERS).document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> orderStatusMessageLiveData.setValue("Status updated to " + newStatus))
                .addOnFailureListener(e -> orderStatusMessageLiveData.setValue("Failed to update status: " + e.getMessage()));
    }

    public void detachListeners() {
        if (studentOrdersListener != null) {
            studentOrdersListener.remove();
        }
        if (adminOrdersListener != null) {
            adminOrdersListener.remove();
        }
    }
}
