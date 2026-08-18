package com.canteen.foodordering.models;

import java.io.Serializable;

public class Coupon implements Serializable {
    private String id;
    private String code;
    private String title;
    private String description;
    private String discountType; // "FLAT" or "PERCENTAGE"
    private double discountValue; // e.g. 50.0 for FLAT or 20.0 for 20%
    private double minOrderAmount; // e.g. 100.0
    private double maxDiscount; // e.g. 100.0 (for PERCENTAGE caps)
    private long expiryTimestamp; // 0 for no expiration
    private boolean active;

    public Coupon() {
        this.active = true;
        this.discountType = "FLAT";
    }

    public Coupon(String id, String code, String title, String description,
                  String discountType, double discountValue, double minOrderAmount,
                  double maxDiscount, long expiryTimestamp, boolean active) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.maxDiscount = maxDiscount;
        this.expiryTimestamp = expiryTimestamp;
        this.active = active;
    }

    public double calculateDiscount(double subtotal) {
        if (subtotal <= 0) return 0.0;
        double discount = 0.0;
        if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
            discount = (subtotal * discountValue) / 100.0;
            if (maxDiscount > 0 && discount > maxDiscount) {
                discount = maxDiscount;
            }
        } else {
            discount = discountValue;
        }
        discount = Math.min(discount, subtotal);
        return Math.round(discount * 100.0) / 100.0;
    }

    public boolean isExpired() {
        return expiryTimestamp > 0 && System.currentTimeMillis() > expiryTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public double getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(double minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public double getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(double maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public void setExpiryTimestamp(long expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
