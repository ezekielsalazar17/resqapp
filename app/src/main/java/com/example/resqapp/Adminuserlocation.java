package com.example.resqapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Adminuserlocation extends AppCompatActivity implements OnMapReadyCallback {

    TextView adminloc, adminlat1, adminlongi,contactNum, useradd, userlat, userlongi;
    Button call1;

    private String adminlocs;
    private String userID;
    private FirebaseAuth fAuth;
    private FirebaseFirestore firestore;

    private static final String TAG = "Adminuserlocation";
    private String phoneNumber;

    private static final long CHECK_INTERVAL = 5000; // Check every 5 seconds
    private Handler checkHandler;
    private Runnable checkRunnable;

    private AlertDialog dialog;
    Geocoder geocoder;
    private GoogleMap gMap;
    private static final int LOCATION_PERMISSION_CODE = 101;

    Handler handler;
    long refreshTime = 5000;
    Runnable runnable;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_location);

        getSupportActionBar().hide();

        adminloc = findViewById(R.id.admin_loc);
        adminlat1 = findViewById(R.id.adminlatt);
        adminlongi = findViewById(R.id.adminlongii);

        contactNum = findViewById(R.id.contact_admin);

        useradd = findViewById(R.id.useraddress);
        userlat = findViewById(R.id.userlatt);
        userlongi = findViewById(R.id.userlongii);

        call1 = findViewById(R.id.call);

        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        geocoder = new Geocoder(this, Locale.getDefault());
        if (isLocationPermissionGranted()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapsuser);
            mapFragment.getMapAsync(this);
        }

        handler = new Handler();
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(runnable, refreshTime);
                isLocationPermissionGranted();
                showLocation();
            }
        }, refreshTime);

        String historyCollection = "firedeptuser";

        // Get current user ID
        userID = fAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch data from Firestore
        db.collection(historyCollection)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        checkIfDocumentDeleted(snapshots);

                        for (QueryDocumentSnapshot document : snapshots) {
                            // Data is fetched, proceed to fetch the rest of the data
                            String address = document.getString("Admin Address");
                            address = capitalizeEveryWord(address); // Capitalize the address
                            String contactNum1 = document.getString("Contact Number");
                            String fetching1 = document.getString("Fetched");

                            Double latitudeObj = document.getDouble("Latitude");
                            Double longitudeObj = document.getDouble("Longitude");

                            double latitude = latitudeObj != null ? latitudeObj.doubleValue() : 0.0;
                            double longitude = longitudeObj != null ? longitudeObj.doubleValue() : 0.0;

                            adminloc.setText(address);
                            adminlat1.setText(String.valueOf(latitude));
                            adminlongi.setText(String.valueOf(longitude));
                            contactNum.setText(contactNum1);
                            break;
                        }

                    }
                });


        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            fetchUserData(userID);
        } else {
            // Redirect user to login screen or handle as per your app's logic
            Toast.makeText(this, "User not authenticated. Redirecting to login screen...", Toast.LENGTH_SHORT).show();
            // Example: startActivity(new Intent(this, LoginActivity.class));
        }


        call1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactNum != null && !contactNum.getText().toString().isEmpty()) {
                    callContactNumber(contactNum.getText().toString());
                } else {
                    Toast.makeText(Adminuserlocation.this, "Contact number not available", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    private void checkIfDocumentDeleted(QuerySnapshot snapshots) {
        if (snapshots.isEmpty()) {
            // Document is deleted or not available
            showPopupDialog();
        }
    }

    private void deleteLastTransaction() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss(); // Dismiss the last dialog if it's still showing
        }
    }


    private void showPopupDialog() {
        // Delete the last transaction before showing a new one
        deleteLastTransaction();

        // Check if the activity is running and not in a finishing or destroyed state
        if (!isFinishing() && !isDestroyed()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("REQUEST");
            builder.setMessage("Your request is done");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Dismiss the dialog before starting a new activity
                    dialog.dismiss();
                    // Proceed to another activity
                    Intent intent = new Intent(Adminuserlocation.this, DashboardUser.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            });
            dialog = builder.create();
            // Show the dialog
            dialog.show();
        }
    }

    private void fetchUserData(String userID) {
        DocumentReference documentReference = firestore.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Firestore Error: " + error.getMessage());
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String addressuser = documentSnapshot.getString("Address");
                    addressuser = capitalizeEveryWord(addressuser); // Capitalize the address

                    Double latitudeObj = documentSnapshot.getDouble("Latitude");
                    Double longitudeObj = documentSnapshot.getDouble("Longitude");

                    double latitudeuser = latitudeObj != null ? latitudeObj.doubleValue() : 0.0;
                    double longitudeuser = longitudeObj != null ? longitudeObj.doubleValue() : 0.0;

                    useradd.setText(addressuser);
                    userlat.setText(String.valueOf(latitudeuser));
                    userlongi.setText(String.valueOf(longitudeuser));

                } else {
                    Log.d(TAG, "No such document");
                }
            }
        });
    }

    private void callContactNumber(String phoneNumber) {
        // Check if phone number is not empty
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            // Create an intent to initiate a phone call
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

            // Check if there is an app that can handle this intent
            if (callIntent.resolveActivity(getPackageManager()) != null) {
                // Start the activity
                startActivity(callIntent);
            } else {
                // No app can handle the intent
                Toast.makeText(Adminuserlocation.this, "No app available to make calls", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Phone number is empty
            Toast.makeText(Adminuserlocation.this, "Contact number not available", Toast.LENGTH_SHORT).show();
        }
    }

    private String capitalizeEveryWord(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Split the text by spaces
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();

        // Capitalize the first letter of each word
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(" ");
            }
        }
        // Remove trailing space
        return result.toString().trim();
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        double latitude1 = Double.parseDouble(adminlat1.getText().toString());
        double longitude1 = Double.parseDouble(adminlongi.getText().toString());

        LatLng adminLatlong = new LatLng(latitude1, longitude1);
        gMap.addMarker(new MarkerOptions().position(adminLatlong).title("Admin"));

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
        }

        gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public void showLocation() {
        if (isLocationPermissionGranted()) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {

                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                userlat.setText("Latitude: " + latitude);
                                userlongi.setText("Longitude: " + longitude);

                                getAddressFromLocation(latitude, longitude);

                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();

                                    firestore = FirebaseFirestore.getInstance();

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
            ActivityCompat.requestPermissions(Adminuserlocation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private Object getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                useradd.setText("Address: " + addressLine);
                return addressLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Override onBackPressed method to disable the back button functionality
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Do nothing (disable back button)
        // Alternatively, you can show a toast message indicating the back button is disabled
        Toast.makeText(this, "Back button disabled in this screen", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        deleteLastTransaction();

    }

    // Override onStop to dismiss dialog when activity is stopped
    @Override
    protected void onStop() {
        super.onStop();
        deleteLastTransaction();
        }

}