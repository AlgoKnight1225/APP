package com.canteen.foodordering.models;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private String studentId;
    private String studentName;
    private String studentPhone;
    private List<CartItem> items;
    private double totalPrice;
    private String status; // "Placed", "Preparing", "Ready", "Delivered"
    private long timestamp;
    private String notes;

    public Order() {
    }

    public Order(String orderId, String studentId, String studentName, String studentPhone,
                 List<CartItem> items, double totalPrice, String status, long timestamp, String notes) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentPhone = studentPhone;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.timestamp = timestamp;
        this.notes = notes;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
