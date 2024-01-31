package com.example.resqapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
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
    ImageButton Profile;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.dashboardfiredepartment);

        email = findViewById(R.id.fire_email1);
         fAuth = FirebaseAuth.getInstance();
         fStore = FirebaseFirestore.getInstance();
         Profile = findViewById(R.id.adminprofile);

         userId = fAuth.getCurrentUser().getUid();

        Profile.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), Fireprofile.class));
        });

    }

}
