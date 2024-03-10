package com.example.resqapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LocationSharingAdmin extends AppCompatActivity {

    TextView userLocation, adminLocation, userlat, userlong, adminlat, adminlong;
    Button direction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_sharing_admin);

        userLocation = findViewById(R.id.user_loc);
        userlat = findViewById(R.id.userlatitudes);
        userlong = findViewById(R.id.userlongitudes);

        adminLocation = findViewById(R.id.admin_loc);
        adminlat = findViewById(R.id.adminlatitudes);
        adminlong = findViewById(R.id.adminlongitudes);

        direction = findViewById(R.id.go_to_gmaps);

        String userloc = getIntent().getStringExtra("Address");
        userLocation.setText(userloc);
        double userloclat = getIntent().getDoubleExtra("Latitude", 0.0);
        userlat.setText(String.valueOf(userloclat));
        double userloclongi = getIntent().getDoubleExtra("Longitude", 0.0);
        userlong.setText(String.valueOf(userloclongi));

        String adminloc = getIntent().getStringExtra("Address Admin");
        adminLocation.setText(adminloc);
        String adminlat1 = getIntent().getStringExtra("Latitude Admin");
        String adminlongi1 = getIntent().getStringExtra("Longitude Admin");
        adminlat.setText(adminlat1);
        adminlong.setText(adminlongi1);

        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LocationSharingAdmin.this, AdminDirections.class);

                intent.putExtra("user_location", userloc);
                intent.putExtra("user_latitude", (userloclat));
                intent.putExtra("user_longitude", (userloclongi));

                intent.putExtra("admin_location", adminloc);
                intent.putExtra("admin_latitude", adminlat1);
                intent.putExtra("admin_longitude", adminlongi1);

                startActivity(intent);

            }
        });


    }
}