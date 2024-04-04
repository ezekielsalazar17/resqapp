package com.example.resqapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
    Handler handler;
    Runnable runnable;
    long refreshTime = 10000;
    private GoogleMap gMap;
    private static final int LOCATION_PERMISSION_CODE = 101;

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

            }
        }, refreshTime);
        updateAdminLocation("Admin");

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

                            Double latitudeObj = document.getDouble("Latitude");
                            Double longitudeObj = document.getDouble("Longitude");

                            double latitude = latitudeObj != null ? latitudeObj.doubleValue() : 0.0;
                            double longitude = longitudeObj != null ? longitudeObj.doubleValue() : 0.0;

                            adminloc.setText(address);
                            adminlat1.setText(String.valueOf(latitude));
                            adminlongi.setText(String.valueOf(longitude));
                            contactNum.setText(contactNum1);
                            updateMarkersOnMap(latitude, longitude);
                            break;
                        }

                    }
                });


        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            fetchUserData(userID);
        } else {
            Toast.makeText(this, "User not authenticated. Redirecting to login screen...", Toast.LENGTH_SHORT).show();
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

    /*private void fetchDataFromFirestore() {

        String historyCollection = "firedeptuser";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Fetch data from Firestore
        db.collection(historyCollection).document(userID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        checkIfDocumentDeleted(documentSnapshot);

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // Data is fetched, proceed to fetch the rest of the data
                            String address = documentSnapshot.getString("Admin Address");
                            address = capitalizeEveryWord(address); // Capitalize the address

                            Double latitudeObj = documentSnapshot.getDouble("Latitude");
                            Double longitudeObj = documentSnapshot.getDouble("Longitude");

                            String contactNum1 = documentSnapshot.getString("Contact Number");

                            double latitude = latitudeObj != null ? latitudeObj.doubleValue() : 0.0;
                            double longitude = longitudeObj != null ? longitudeObj.doubleValue() : 0.0;

                            adminloc.setText(address);
                            adminlat1.setText(String.valueOf(latitude));
                            adminlongi.setText(String.valueOf(longitude));
                            contactNum.setText(contactNum1);

                            // Update markers on the map
                            updateMarkersOnMap(latitude, longitude);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    }
                });

    }*/

    private void updateMarkersOnMap(double latitude, double longitude) {
        if (gMap != null) {
            LatLng adminLatlong = new LatLng(latitude, longitude);
            // Clear previous markers
            gMap.clear();
            // Add new marker for admin
            gMap.addMarker(new MarkerOptions().position(adminLatlong).title("Admin"));
        }
    }

    private void updateAdminLocation(String address) {
        adminloc.setText(address);
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
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

        gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapsuser);
        View mapView = mapFragment.getView();

        if (mapView != null) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    updateAdminMarker();
                }
            });
        }

        if (adminlat1.getText() != null && adminlongi.getText() != null) {
            double latitude = Double.parseDouble(adminlat1.getText().toString());
            double longitude = Double.parseDouble(adminlongi.getText().toString());
            updateMarkersOnMap(latitude, longitude);
        }
    }

    private void updateAdminMarker() {
        if (adminlat1.getText() != null && adminlongi.getText() != null
                && userlat.getText() != null && userlongi.getText() != null) {
            double latitude1 = Double.parseDouble(adminlat1.getText().toString());
            double longitude1 = Double.parseDouble(adminlongi.getText().toString());

            double latitudeuser = Double.parseDouble(userlat.getText().toString());
            double longitudeuser = Double.parseDouble(userlongi.getText().toString());

            LatLng adminLatlong = new LatLng(latitude1, longitude1);
            LatLng userLatlong = new LatLng(latitudeuser, longitudeuser);

            // Clear previous markers
          //  gMap.clear();

            // Add new markers
            gMap.addMarker(new MarkerOptions().position(adminLatlong).title("Admin"));
            gMap.addMarker(new MarkerOptions().position(userLatlong).title("YOU"));

            // Calculate bounds that include both markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(adminLatlong);
            builder.include(userLatlong);
            LatLngBounds bounds = builder.build();

            // Set padding for the bounds, adjust as needed
            int padding = 100; // in pixels

            // Move camera to fit both markers and padding
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            gMap.animateCamera(cameraUpdate);
            getRouteCoordinates(adminLatlong, userLatlong);
        }
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /*@SuppressLint("MissingPermission")
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
    }*/

    private void getRouteCoordinates(LatLng start, LatLng end) {
        String apiKey = "AIzaSyC7m6Ggei6Q9SysKXbmqEHPt-W_nyQ42Vg";

        String urlString = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + start.latitude + "," + start.longitude +
                "&destination=" + end.latitude + "," + end.longitude +
                "&key=" + apiKey;

        new DirectionsTask().execute(urlString);
    }

    private void drawRoute(List<LatLng> routeCoordinates) {
        if (routeCoordinates != null && !routeCoordinates.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(routeCoordinates);
            polylineOptions.width(10);
            polylineOptions.color(Color.BLUE);
            gMap.addPolyline(polylineOptions);
        } else {
            Log.e("AdminDirections", "Route coordinates are empty");
        }
    }


    private class DirectionsTask extends AsyncTask<String, Void, List<LatLng>> {

        @Override
        protected List<LatLng> doInBackground(String... strings) {
            List<LatLng> coordinates = new ArrayList<>();
            HttpURLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                    JsonArray routes = jsonResponse.getAsJsonArray("routes");
                    if (routes.size() > 0) {
                        for (JsonElement routeElement : routes) {
                            JsonObject route = routeElement.getAsJsonObject();
                            JsonObject overviewPolyline = route.getAsJsonObject("overview_polyline");
                            String encodedPolyline = overviewPolyline.get("points").getAsString();
                            List<LatLng> decodedPolyline = decodePoly(encodedPolyline);
                            coordinates.addAll(decodedPolyline);
                        }
                    } else {
                        Log.e("DirectionsTask", "No routes found in the response.");
                    }
                } else {
                    Log.e("DirectionsTask", "HTTP error: " + connection.getResponseCode() + " " + connection.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("DirectionsTask", "Error occurred while fetching directions: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return coordinates;
        }

        @Override
        protected void onPostExecute(List<LatLng> routeCoordinates) {
            if (!routeCoordinates.isEmpty()) {
                drawRoute(routeCoordinates);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng latLng : routeCoordinates) {
                    builder.include(latLng);
                }
                LatLngBounds bounds = builder.build();
                int padding = 100;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                gMap.animateCamera(cu);
            } else {
                Log.e("DirectionsTask", "Route coordinates are empty.");
            }
        }
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }
        return poly;
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