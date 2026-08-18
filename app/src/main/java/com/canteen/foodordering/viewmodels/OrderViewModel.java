package com.canteen.foodordering.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.canteen.foodordering.models.Order;
import com.canteen.foodordering.repositories.OrderRepository;

import java.util.List;

public class OrderViewModel extends AndroidViewModel {
    private final OrderRepository orderRepository;

    public OrderViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository();
    }

    public LiveData<List<Order>> getStudentOrdersLiveData() {
        return orderRepository.getStudentOrdersLiveData();
    }

    public LiveData<List<Order>> getAdminOrdersLiveData() {
        return orderRepository.getAdminOrdersLiveData();
    }

    public LiveData<String> getOrderStatusMessageLiveData() {
        return orderRepository.getOrderStatusMessageLiveData();
    }

    public void listenToStudentOrders() {
        orderRepository.listenToStudentOrders();
    }

    public void listenToAllAdminOrders() {
        orderRepository.listenToAllAdminOrders();
    }

    public void placeOrder(Order order) {
        orderRepository.placeOrder(order);
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        orderRepository.updateOrderStatus(orderId, newStatus);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        orderRepository.detachListeners();
    }
}
