package com.example.resqapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AccessLocUser extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;

    TextView lat, longi, address;
    Button showLocationButton;
    Geocoder geocoder;
    List<Address> listGeocoder;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;

    private static final int LOCATION_PERMISSION_CODE = 101;
    private TextView addressTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accesslocuser);
        FirebaseApp.initializeApp(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        lat = findViewById(R.id.show_latitude);
        longi = findViewById(R.id.show_longitude);
        addressTextView = findViewById(R.id.show_address); // Add new TextView for address

        showLocationButton = findViewById(R.id.showLocation);
        showLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocation();
            }
        });
        geocoder = new Geocoder(this, Locale.getDefault());
        if (isLocationPermissionGranted()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        LatLng home = new LatLng(14.5009475, 120.9951491);
        gMap.addMarker(new MarkerOptions().position(home).title("Home"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 15));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
        }

        gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public void showLocation() {
        if (isLocationPermissionGranted()) {
            // Use FusedLocationProviderClient to get the user's current location
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Get the latitude and longitude of the user's current location
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                // Update the TextViews with the new location
                                lat.setText("Latitude: " + latitude);
                                longi.setText("Longitude: " + longitude);


                                // Get the address from latitude and longitude
                                getAddressFromLocation(latitude, longitude);

                                // Add marker to the map at the user's current location
                                LatLng userLocation = new LatLng(latitude, longitude);
                                gMap.clear(); // Clear existing markers
                                gMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();

                                    firestore = FirebaseFirestore.getInstance(); // Get Firestore instance

                                    firestore.collection("users").document(userId)
                                            .update("Latitude", latitude, "Longitude", longitude, "Address", getAddressFromLocation(latitude, longitude))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("Firebase", "Latitude and longitude stored successfully");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("Firebase", "Error storing latitude and longitude", e);
                                                }
                                            });
                                } else {
                                    // Handle case where user is not authenticated
                                    Log.e("Firebase", "User not authenticated");
                                }
                            } else {
                                // Request location permission if not granted
                                // ...
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(AccessLocUser.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private Object getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                addressTextView.setText("Address: " + addressLine);
                return addressLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
