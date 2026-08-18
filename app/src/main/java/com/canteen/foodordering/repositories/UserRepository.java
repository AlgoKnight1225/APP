package com.canteen.foodordering.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.canteen.foodordering.models.User;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final FirebaseFirestore db;
    private final MutableLiveData<List<User>> usersLiveData;
    private final MutableLiveData<String> errorLiveData;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
        usersLiveData = new MutableLiveData<>(new ArrayList<>());
        errorLiveData = new MutableLiveData<>();
    }

    public LiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void fetchAllUsers() {
        db.collection(Constants.COLLECTION_USERS)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        errorLiveData.setValue("Error fetching users: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<User> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                user.setUid(doc.getId());
                                list.add(user);
                            }
                        }
                        usersLiveData.setValue(list);
                    }
                });
    }
}
