package com.example.resqapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Adminuserlocation extends AppCompatActivity {

    TextView adminloc, adminlat1, adminlongi;

    private String adminlocs;
    private String userID;
    private FirebaseAuth fAuth;
    private FirebaseFirestore firestore;

    private static final String TAG = "Adminuserlocation";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_location);

        getSupportActionBar().hide();

        adminloc = findViewById(R.id.admin_loc);
        adminlat1 = findViewById(R.id.adminlatt);
        adminlongi = findViewById(R.id.adminlongii);
        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        String adminloc1 = getIntent().getStringExtra("admin_add");
        adminloc.setText(adminloc1);
        String adminlat12 = getIntent().getStringExtra("admin_lat");
        adminlat1.setText(adminlat12);
        String adminlongi1 = getIntent().getStringExtra("admin_longi");
        adminlongi.setText(adminlongi1);
    }


    // Override onBackPressed method to disable the back button functionality
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Do nothing (disable back button)
        // Alternatively, you can show a toast message indicating the back button is disabled
        Toast.makeText(this, "Back button disabled in this screen", Toast.LENGTH_SHORT).show();
    }
}