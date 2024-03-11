package com.example.resqapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class AdminDirections extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMapadmin;;
    private LatLng userLocation;
    private LatLng adminLocation;

    TextView lat, longi, latadmin1, longiadmin1;

    Geocoder geocoder;
    List<Address> listGeocoder;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directions_admin);

        getSupportActionBar().hide();

        lat = findViewById(R.id.userlati1);
        longi = findViewById(R.id.userlongi1);

        latadmin1 = findViewById(R.id.adminlat1);
        longiadmin1 = findViewById(R.id.adminlongi1);

        userLocation = getIntent().getParcelableExtra("user_location");
        double lat1 = getIntent().getDoubleExtra("user_latitude", 0.0);
        lat.setText(String.valueOf(lat1));
        double lat2 = getIntent().getDoubleExtra("user_longitude", 0.0);
        longi.setText(String.valueOf(lat2));

        adminLocation = getIntent().getParcelableExtra("admin_location");
        String latadmins = getIntent().getStringExtra("admin_latitude");
        latadmin1.setText(latadmins);
        String longiadmins = getIntent().getStringExtra("admin_longitude");
        longiadmin1.setText(String.valueOf(longiadmins));



        geocoder = new Geocoder(this, Locale.getDefault());
        if (isLocationPermissionGranted()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        gMapadmin = googleMap;

        double latitude = Double.parseDouble(lat.getText().toString());
        double longitude = Double.parseDouble(longi.getText().toString());

        double latitude1 = Double.parseDouble(latadmin1.getText().toString());
        double longitude1 = Double.parseDouble(longiadmin1.getText().toString());

        LatLng userLatlong = new LatLng(latitude, longitude);
        gMapadmin.addMarker(new MarkerOptions().position(userLatlong).title("Emergency Location (User)"));
        gMapadmin.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatlong, 15));

        // Checking for permission before enabling location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gMapadmin.setMyLocationEnabled(true);
        } else {
            // Handle the case where permission is not granted
            // You can request permission here or handle it accordingly
            // For simplicity, I'm assuming you have already requested permission somewhere else
        }

        gMapadmin.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

}