package com.canteen.foodordering.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.canteen.foodordering.models.User;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final MutableLiveData<FirebaseUser> firebaseUserLiveData;
    private final MutableLiveData<User> userProfileLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUserLiveData = new MutableLiveData<>(auth.getCurrentUser());
        userProfileLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>(false);
    }

    public LiveData<FirebaseUser> getFirebaseUserLiveData() {
        return firebaseUserLiveData;
    }

    public LiveData<User> getUserProfileLiveData() {
        return userProfileLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fUser = authResult.getUser();
                    if (fUser != null) {
                        firebaseUserLiveData.setValue(fUser);
                        fetchUserProfile(fUser.getUid());
                    } else {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Login failed: User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void register(String name, String email, String phone, String password, String role) {
        loadingLiveData.setValue(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fUser = authResult.getUser();
                    if (fUser != null) {
                        User newUser = new User(fUser.getUid(), name, email, phone, role);
                        db.collection(Constants.COLLECTION_USERS).document(fUser.getUid())
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    loadingLiveData.setValue(false);
                                    firebaseUserLiveData.setValue(fUser);
                                    userProfileLiveData.setValue(newUser);
                                })
                                .addOnFailureListener(e -> {
                                    loadingLiveData.setValue(false);
                                    errorLiveData.setValue("Failed to save user profile: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void fetchUserProfile(String uid) {
        if (uid == null) return;
        loadingLiveData.setValue(true);
        db.collection(Constants.COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    loadingLiveData.setValue(false);
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setLastLogin(System.currentTimeMillis());
                            db.collection(Constants.COLLECTION_USERS).document(uid).update("lastLogin", System.currentTimeMillis());
                            userProfileLiveData.setValue(user);
                        }
                    } else {
                        errorLiveData.setValue("User profile not found in database");
                    }
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void updateProfile(String name, String phone, String profileImage) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        
        loadingLiveData.setValue(true);
        String uid = currentUser.getUid();
        db.collection(Constants.COLLECTION_USERS).document(uid)
                .update("name", name, "phone", phone, "profileImage", profileImage)
                .addOnSuccessListener(aVoid -> {
                    fetchUserProfile(uid);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void sendPasswordResetEmail(String email) {
        loadingLiveData.setValue(true);
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue("RESET_SUCCESS");
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void logout() {
        auth.signOut();
        firebaseUserLiveData.setValue(null);
        userProfileLiveData.setValue(null);
    }
}
