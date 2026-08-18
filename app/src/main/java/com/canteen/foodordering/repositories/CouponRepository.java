package com.canteen.foodordering.repositories;

import com.canteen.foodordering.models.Coupon;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CouponRepository {
    private final FirebaseFirestore db;
    private final Map<String, Coupon> defaultCoupons;

    public interface CouponValidationCallback {
        void onSuccess(Coupon coupon, double discountAmount, String message);
        void onError(String errorMessage);
    }

    public CouponRepository() {
        db = FirebaseFirestore.getInstance();
        defaultCoupons = new HashMap<>();
        initDefaultCoupons();
    }

    private void initDefaultCoupons() {
        // Pre-configured campus coupons that work out of the box
        defaultCoupons.put("FIRST50", new Coupon(
                "FIRST50", "FIRST50", "Flat ₹50 OFF",
                "Flat ₹50 discount on orders above ₹150",
                "FLAT", 50.0, 150.0, 50.0, 0, true
        ));

        defaultCoupons.put("BREW20", new Coupon(
                "BREW20", "BREW20", "20% OFF",
                "20% discount on coffee & cafe favorites up to ₹60",
                "PERCENTAGE", 20.0, 80.0, 60.0, 0, true
        ));

        defaultCoupons.put("WELCOME10", new Coupon(
                "WELCOME10", "WELCOME10", "10% OFF",
                "10% discount on entire canteen order",
                "PERCENTAGE", 10.0, 50.0, 50.0, 0, true
        ));

        defaultCoupons.put("FIRSTBITE", new Coupon(
                "FIRSTBITE", "FIRSTBITE", "Flat ₹30 OFF",
                "Flat ₹30 discount on orders above ₹100",
                "FLAT", 30.0, 100.0, 30.0, 0, true
        ));

        defaultCoupons.put("MEALCOMBO", new Coupon(
                "MEALCOMBO", "MEALCOMBO", "Flat ₹40 OFF",
                "Special ₹40 off on combos and meals above ₹120",
                "FLAT", 40.0, 120.0, 40.0, 0, true
        ));

        defaultCoupons.put("FRESHBAKE", new Coupon(
                "FRESHBAKE", "FRESHBAKE", "15% OFF Bakery",
                "15% discount on artisanal bakery items",
                "PERCENTAGE", 15.0, 60.0, 50.0, 0, true
        ));
    }

    public void validateCoupon(String rawCode, double subtotal, CouponValidationCallback callback) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            callback.onError("Please enter a coupon code");
            return;
        }

        final String code = rawCode.trim().toUpperCase();

        // 1. First check Firestore database collection "coupons"
        db.collection(Constants.COLLECTION_COUPONS)
                .whereEqualTo("code", code)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        Coupon coupon = doc.toObject(Coupon.class);
                        if (coupon != null) {
                            coupon.setId(doc.getId());
                            evaluateCoupon(coupon, subtotal, callback);
                            return;
                        }
                    }

                    // Also check document by ID directly
                    db.collection(Constants.COLLECTION_COUPONS)
                            .document(code)
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (doc != null && doc.exists()) {
                                    Coupon coupon = doc.toObject(Coupon.class);
                                    if (coupon != null) {
                                        coupon.setId(doc.getId());
                                        evaluateCoupon(coupon, subtotal, callback);
                                        return;
                                    }
                                }

                                // 2. Fallback to default verified coupons
                                if (defaultCoupons.containsKey(code)) {
                                    Coupon defaultCoupon = defaultCoupons.get(code);
                                    evaluateCoupon(defaultCoupon, subtotal, callback);
                                } else {
                                    callback.onError("Invalid coupon code");
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Fallback to default on network failure
                                if (defaultCoupons.containsKey(code)) {
                                    Coupon defaultCoupon = defaultCoupons.get(code);
                                    evaluateCoupon(defaultCoupon, subtotal, callback);
                                } else {
                                    callback.onError("Invalid coupon code");
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    // Fallback to default on network failure
                    if (defaultCoupons.containsKey(code)) {
                        Coupon defaultCoupon = defaultCoupons.get(code);
                        evaluateCoupon(defaultCoupon, subtotal, callback);
                    } else {
                        callback.onError("Invalid coupon code");
                    }
                });
    }

    private void evaluateCoupon(Coupon coupon, double subtotal, CouponValidationCallback callback) {
        if (coupon == null) {
            callback.onError("Invalid coupon code");
            return;
        }

        if (!coupon.isActive()) {
            callback.onError("This coupon is expired or unavailable.");
            return;
        }

        if (coupon.isExpired()) {
            callback.onError("This coupon is expired or unavailable.");
            return;
        }

        if (subtotal < coupon.getMinOrderAmount()) {
            callback.onError(String.format("Minimum order amount of ₹%.0f required for this coupon.", coupon.getMinOrderAmount()));
            return;
        }

        double discount = coupon.calculateDiscount(subtotal);
        if (discount <= 0) {
            callback.onError("Coupon discount could not be applied to this cart.");
            return;
        }

        callback.onSuccess(coupon, discount, "Coupon applied successfully! 🎉");
    }

    public List<Coupon> getSuggestedCoupons() {
        return new ArrayList<>(defaultCoupons.values());
    }
}
