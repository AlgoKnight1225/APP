package com.canteen.foodordering.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.canteen.foodordering.admin.AdminMainActivity;
import com.canteen.foodordering.models.User;
import com.canteen.foodordering.student.StudentMainActivity;
import com.canteen.foodordering.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        checkUserSession();
    }

    private void checkUserSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        } else {
            db.collection(Constants.COLLECTION_USERS)
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null && Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
                                startActivity(new Intent(SplashActivity.this, AdminMainActivity.class));
                            } else {
                                startActivity(new Intent(SplashActivity.this, StudentMainActivity.class));
                            }
                        } else {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    });
        }
    }
}
