package com.example.resqapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LocationSharingAdmin extends AppCompatActivity {

    TextView userLocation, adminLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_sharing_admin);

        userLocation = findViewById(R.id.user_loc);
        adminLocation = findViewById(R.id.admin_loc);

        String userloc = getIntent().getStringExtra("Address");
        if (userloc != null) {
            userLocation.setText(userloc);
        } else {
            userLocation.setText("User Location Not Available");
        }

        String adminloc = getIntent().getStringExtra("Admin Address");
        if (adminloc != null) {
            adminLocation.setText(adminloc);
        } else {
            adminLocation.setText("Admin Location Not Available");
        }
    }

}