package com.canteen.foodordering.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.canteen.foodordering.models.FoodItem;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FoodRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<List<FoodItem>> foodItemsLiveData;
    private final MutableLiveData<String> statusMessageLiveData;
    private ListenerRegistration foodListener;

    public FoodRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        foodItemsLiveData = new MutableLiveData<>(new ArrayList<>());
        statusMessageLiveData = new MutableLiveData<>();
        listenToFoodItems();
    }

    public LiveData<List<FoodItem>> getFoodItemsLiveData() {
        return foodItemsLiveData;
    }

    public LiveData<String> getStatusMessageLiveData() {
        return statusMessageLiveData;
    }

    public void listenToFoodItems() {
        if (foodListener != null) {
            foodListener.remove();
        }

        foodListener = db.collection(Constants.COLLECTION_FOOD)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        statusMessageLiveData.setValue("Error loading food items: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<FoodItem> items = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            FoodItem item = doc.toObject(FoodItem.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                items.add(item);
                            }
                        }
                        foodItemsLiveData.setValue(items);
                    }
                });
    }

    public void addFoodItem(FoodItem foodItem) {
        String docId = db.collection(Constants.COLLECTION_FOOD).document().getId();
        foodItem.setId(docId);
        db.collection(Constants.COLLECTION_FOOD).document(docId)
                .set(foodItem)
                .addOnSuccessListener(aVoid -> statusMessageLiveData.setValue("Food item added successfully"))
                .addOnFailureListener(e -> statusMessageLiveData.setValue("Failed to add item: " + e.getMessage()));
    }

    public void updateFoodItem(FoodItem foodItem) {
        if (foodItem == null || foodItem.getId() == null) return;
        db.collection(Constants.COLLECTION_FOOD).document(foodItem.getId())
                .set(foodItem)
                .addOnSuccessListener(aVoid -> statusMessageLiveData.setValue("Food item updated successfully"))
                .addOnFailureListener(e -> statusMessageLiveData.setValue("Failed to update item: " + e.getMessage()));
    }

    public void deleteFoodItem(String foodId) {
        if (foodId == null) return;
        db.collection(Constants.COLLECTION_FOOD).document(foodId)
                .delete()
                .addOnSuccessListener(aVoid -> statusMessageLiveData.setValue("Food item deleted successfully"))
                .addOnFailureListener(e -> statusMessageLiveData.setValue("Failed to delete item: " + e.getMessage()));
    }

    public void toggleFavorite(String foodId, boolean currentFavorite) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || foodId == null) return;

        String uid = user.getUid();
        if (currentFavorite) {
            db.collection(Constants.COLLECTION_USERS).document(uid)
                    .update("favoriteIds", FieldValue.arrayRemove(foodId));
        } else {
            db.collection(Constants.COLLECTION_USERS).document(uid)
                    .update("favoriteIds", FieldValue.arrayUnion(foodId));
        }
    }

    public void detachListener() {
        if (foodListener != null) {
            foodListener.remove();
        }
    }
}
