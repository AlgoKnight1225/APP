package com.canteen.foodordering.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.canteen.foodordering.models.User;
import com.canteen.foodordering.repositories.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
    }

    public LiveData<FirebaseUser> getFirebaseUserLiveData() {
        return authRepository.getFirebaseUserLiveData();
    }

    public LiveData<User> getUserProfileLiveData() {
        return authRepository.getUserProfileLiveData();
    }

    public LiveData<String> getErrorLiveData() {
        return authRepository.getErrorLiveData();
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return authRepository.getLoadingLiveData();
    }

    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public void login(String email, String password) {
        authRepository.login(email, password);
    }

    public void register(String name, String email, String phone, String password, String role) {
        authRepository.register(name, email, phone, password, role);
    }

    public void fetchUserProfile(String uid) {
        authRepository.fetchUserProfile(uid);
    }

    public void updateProfile(String name, String phone, String profileImage) {
        authRepository.updateProfile(name, phone, profileImage);
    }

    public void sendPasswordResetEmail(String email) {
        authRepository.sendPasswordResetEmail(email);
    }

    public void logout() {
        authRepository.logout();
    }
}
