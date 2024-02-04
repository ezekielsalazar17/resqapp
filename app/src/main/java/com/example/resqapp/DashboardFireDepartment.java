package com.example.resqapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardFireDepartment extends AppCompatActivity {

    TextView email;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    ImageButton profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ... (Window configuration code remains the same)

        setContentView(R.layout.dashboardfiredepartment);

        email = findViewById(R.id.fire_email1);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        profileButton = findViewById(R.id.adminprofile); // Corrected variable name

        // Check for user authentication before accessing userId
        if (fAuth.getCurrentUser() != null) {
            userId = fAuth.getCurrentUser().getUid();
        } else {
            // Handle the case where the user is not authenticated
            // (e.g., redirect to login or display an appropriate message)
        }

        profileButton.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), Fireprofile.class));
        });
    }
}