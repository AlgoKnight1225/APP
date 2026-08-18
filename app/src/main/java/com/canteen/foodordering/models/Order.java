package com.canteen.foodordering.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private String studentId;
    private String studentName;
    private String studentPhone;
    private List<CartItem> items;
    private double totalPrice;
    private double subtotal;
    private double tax;
    private String status; // "PENDING", "PREPARING", "READY", "COMPLETED", "CANCELLED"
    private long timestamp;
    private String notes;
    private String paymentMethod;
    private String couponCode;
    private double discount;

    public Order() {
        this.items = new ArrayList<>();
        this.status = "PENDING";
        this.timestamp = System.currentTimeMillis();
        this.paymentMethod = "Cash";
        this.couponCode = "";
        this.discount = 0.0;
    }

    public Order(String orderId, String studentId, String studentName, String studentPhone,
                 List<CartItem> items, double totalPrice, double subtotal, double tax,
                 String status, long timestamp, String notes, String paymentMethod) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentPhone = studentPhone;
        this.items = items;
        this.totalPrice = totalPrice;
        this.subtotal = subtotal;
        this.tax = tax;
        this.status = status;
        this.timestamp = timestamp;
        this.notes = notes;
        this.paymentMethod = paymentMethod;
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
        if (items == null) {
            items = new ArrayList<>();
        }
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

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
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

    public String getPaymentMethod() {
        return paymentMethod == null ? "Cash" : paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }
}
