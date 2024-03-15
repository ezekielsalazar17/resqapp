package com.example.resqapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class DashboardUser extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    private static final int LOCATION_PERMISSION_CODE = 101;
    private static String TAG;
    Button locationSharing;
    ImageButton firebutton, profilebutton, policebutton, coastbutton, ambulancebutton;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;
    private String userID;
    private String addadmin;
    private String latadmin;
    private String longiadmin;
    private MyAdapter.LocationFetchListener locationFetchListener;


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.dashboarduser);

        locationSharing = findViewById(R.id.location_tracking);
        firebutton = findViewById(R.id.fire_button);
        policebutton = findViewById(R.id.police_button);
        profilebutton = findViewById(R.id.profile_button);
        coastbutton = findViewById(R.id.coastguard_button);
        ambulancebutton = findViewById(R.id.ambulance_button);

        locationSharing.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), AccessLocUser.class));
        });

        profilebutton.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), UserProfile.class));
        });

        // Call OnGPS method
        OnGPS();
        // Call requestLocationPermission method
        requestLocationPermission();

        fAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        firestore = FirebaseFirestore.getInstance(); // Initialize FirebaseFirestore

        // Set OnClickListener for firebutton
        firebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProgressDialog progressDialog = new ProgressDialog(DashboardUser.this);
                progressDialog.setMessage("Sending...");
                progressDialog.setCancelable(true);
                progressDialog.show();

                // Get Firebase instance
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Define the collection name for user data
                String userCollection = "users";

                // Get the current user ID (assuming you have it)
                String userID = getCurrentUserID(); // Replace with your method to get the user ID

                // Retrieve user's first name and last name from the "users" collection
                db.collection(userCollection)
                        .document(userID)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    // Get user's first name and last name
                                    String firstName = documentSnapshot.getString("First Name");
                                    String lastName = documentSnapshot.getString("Last Name");
                                    String address = documentSnapshot.getString("Address");
                                    Double latitude = documentSnapshot.getDouble("Latitude");
                                    Double longitude = documentSnapshot.getDouble("Longitude");
                                    String contactNum = documentSnapshot.getString("Contact Number");

                                    // Create a new document in the "History" collection with user's first name and last name
                                    Map<String, Object> historyData = new HashMap<>();
                                    historyData.put("firstName", firstName);
                                    historyData.put("lastName", lastName);
                                    historyData.put("address", address);
                                    historyData.put("latitude", latitude);
                                    historyData.put("longitude", longitude);
                                    historyData.put("contactNum", contactNum);

                                    db.collection("pendingfiredept")
                                            .add(historyData)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressDialog.dismiss();
                                                    Log.d(TAG, "Document added to collection 'pendingfiredept' with ID: " + documentReference.getId());
                                                    startActivity(new Intent(DashboardUser.this, Adminuserlocation.class));
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Error adding document to collection 'History': " + e.getMessage());
                                                    // Handle failure
                                                }
                                            });
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error getting user document: " + e.getMessage());
                                // Handle failure
                            }
                        });

            }
        });
        policebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProgressDialog progressDialog = new ProgressDialog(DashboardUser.this);
                progressDialog.setMessage("Sending...");
                progressDialog.setCancelable(true);
                progressDialog.show();

                // Get Firebase instance
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Define the collection name for user data
                String userCollection = "users";

                // Get the current user ID (assuming you have it)
                String userID = getCurrentUserID(); // Replace with your method to get the user ID

                // Retrieve user's first name and last name from the "users" collection
                db.collection(userCollection)
                        .document(userID)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    // Get user's first name and last name
                                    String firstName = documentSnapshot.getString("First Name");
                                    String lastName = documentSnapshot.getString("Last Name");
                                    String address = documentSnapshot.getString("Address");
                                    Double latitude = documentSnapshot.getDouble("Latitude");
                                    Double longitude = documentSnapshot.getDouble("Longitude");
                                    String contactNum = documentSnapshot.getString("Contact Number");

                                    // Create a new document in the "History" collection with user's first name and last name
                                    Map<String, Object> historyData = new HashMap<>();
                                    historyData.put("firstName", firstName);
                                    historyData.put("lastName", lastName);
                                    historyData.put("address", address);
                                    historyData.put("latitude", latitude);
                                    historyData.put("longitude", longitude);
                                    historyData.put("contactNum", contactNum);


                                    db.collection("pendingpolicedept")
                                            .add(historyData)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressDialog.dismiss();
                                                    Log.d(TAG, "Document added to collection 'pendingpolicedept' with ID: " + documentReference.getId());
                                                    // Perform any additional actions if needed

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Error adding document to collection 'History': " + e.getMessage());
                                                    // Handle failure
                                                }
                                            });
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error getting user document: " + e.getMessage());
                                // Handle failure
                            }
                        });
            }
        });
        coastbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProgressDialog progressDialog = new ProgressDialog(DashboardUser.this);
                progressDialog.setMessage("Sending...");
                progressDialog.setCancelable(true);
                progressDialog.show();

                // Get Firebase instance
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Define the collection name for user data
                String userCollection = "users";

                // Get the current user ID (assuming you have it)
                String userID = getCurrentUserID(); // Replace with your method to get the user ID

                // Retrieve user's first name and last name from the "users" collection
                db.collection(userCollection)
                        .document(userID)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    // Get user's first name and last name
                                    String firstName = documentSnapshot.getString("First Name");
                                    String lastName = documentSnapshot.getString("Last Name");
                                    String address = documentSnapshot.getString("Address");
                                    Double latitude = documentSnapshot.getDouble("Latitude");
                                    Double longitude = documentSnapshot.getDouble("Longitude");
                                    String contactNum = documentSnapshot.getString("Contact Number");

                                    // Create a new document in the "History" collection with user's first name and last name
                                    Map<String, Object> historyData = new HashMap<>();
                                    historyData.put("firstName", firstName);
                                    historyData.put("lastName", lastName);
                                    historyData.put("address", address);
                                    historyData.put("latitude", latitude);
                                    historyData.put("longitude", longitude);
                                    historyData.put("contactNum", contactNum);


                                    db.collection("pendingcoastdept")
                                            .add(historyData)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressDialog.dismiss();
                                                    Log.d(TAG, "Document added to collection 'pendingcoastdept' with ID: " + documentReference.getId());
                                                    // Perform any additional actions if needed

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Error adding document to collection 'History': " + e.getMessage());
                                                    // Handle failure
                                                }
                                            });
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error getting user document: " + e.getMessage());
                                // Handle failure
                            }
                        });
            }
        });
        ambulancebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProgressDialog progressDialog = new ProgressDialog(DashboardUser.this);
                progressDialog.setMessage("Sending...");
                progressDialog.setCancelable(true);
                progressDialog.show();

                // Get Firebase instance
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Define the collection name for user data
                String userCollection = "users";

                // Get the current user ID (assuming you have it)
                String userID = getCurrentUserID(); // Replace with your method to get the user ID

                // Retrieve user's first name and last name from the "users" collection
                db.collection(userCollection)
                        .document(userID)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    // Get user's first name and last name
                                    String firstName = documentSnapshot.getString("First Name");
                                    String lastName = documentSnapshot.getString("Last Name");
                                    String address = documentSnapshot.getString("Address");
                                    Double latitude = documentSnapshot.getDouble("Latitude");
                                    Double longitude = documentSnapshot.getDouble("Longitude");
                                    String contactNum = documentSnapshot.getString("Contact Number");

                                    // Create a new document in the "History" collection with user's first name and last name
                                    Map<String, Object> historyData = new HashMap<>();
                                    historyData.put("firstName", firstName);
                                    historyData.put("lastName", lastName);
                                    historyData.put("address", address);
                                    historyData.put("latitude", latitude);
                                    historyData.put("longitude", longitude);
                                    historyData.put("contactNum", contactNum);


                                    db.collection("pendingambulancedept")
                                            .add(historyData)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressDialog.dismiss();
                                                    Log.d(TAG, "Document added to collection 'pendingambulancedept' with ID: " + documentReference.getId());
                                                    // Perform any additional actions if needed

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Error adding document to collection 'History': " + e.getMessage());
                                                    // Handle failure
                                                }
                                            });
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error getting user document: " + e.getMessage());
                                // Handle failure
                            }
                        });
            }
        });
    }

    private String getCurrentUserID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is signed in
        if (user != null) {
            // Get the user's ID
            return user.getUid();
        } else {
            // User is not signed in, handle this case according to your application logic
            return null;
        }
    }


    private void OnGPS() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            // GPS is not enabled, prompt the user to enable it
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("GPS is not enabled. Do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Open location settings
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            // GPS is enabled, check location permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_CODE);
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, check if GPS is enabled
                OnGPS();
            } else {
                // Location permission denied, handle this case
                // For example, display a message to the user
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}