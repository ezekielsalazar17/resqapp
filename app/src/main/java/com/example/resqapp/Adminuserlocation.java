package com.example.resqapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

public class Adminuserlocation extends AppCompatActivity {

    TextView adminloc, adminlat1, adminlongi,contactNum, useradd, userlat, userlongi, fetched1;
    Button checkAdminloc, call1, done1;

    private String adminlocs;
    private String userID;
    private FirebaseAuth fAuth;
    private FirebaseFirestore firestore;

    private static final String TAG = "Adminuserlocation";
    private String phoneNumber;

    private static final long CHECK_INTERVAL = 5000; // Check every 5 seconds
    private Handler checkHandler;
    private Runnable checkRunnable;

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
        done1 = findViewById(R.id.done);

        fetched1 = findViewById(R.id.fetch);

        checkAdminloc = findViewById(R.id.go_to_gmaps);
        call1 = findViewById(R.id.call);

        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

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

                        for (QueryDocumentSnapshot document : snapshots) {
                            String fetched = document.getString("Fetched");
                            if (fetched == "true") {
                                // Data is fetched, proceed to fetch the rest of the  data
                                String address = document.getString("Admin Address");
                                address = capitalizeEveryWord(address); // Capitalize the address
                                String contactNum1 = document.getString("Contact Number");
                                String fetching1 = document.getString("Fetched");
                                String department = document.getString("Department");

                                Double latitudeObj = document.getDouble("Latitude");
                                Double longitudeObj = document.getDouble("Longitude");

                                double latitude = latitudeObj != null ? latitudeObj.doubleValue() : 0.0;
                                double longitude = longitudeObj != null ? longitudeObj.doubleValue() : 0.0;

                                adminloc.setText(address);
                                adminlat1.setText(String.valueOf(latitude));
                                adminlongi.setText(String.valueOf(longitude));
                                contactNum.setText(contactNum1);
                                fetched1.setText(fetching1);

                                break;
                            }else{
                                Toast.makeText(Adminuserlocation.this, "Theres no admin response", Toast.LENGTH_SHORT).show();
                            }
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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Schedule the next execution after .5 seconds
                handler.postDelayed(this, 3000);
            }
        }, 1000); // .5 seconds delay


        checkAdminloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double adminLat = Double.parseDouble(adminlat1.getText().toString());
                double adminLon = Double.parseDouble(adminlongi.getText().toString());

                double userLat = Double.parseDouble(userlat.getText().toString());
                double userLon = Double.parseDouble(userlongi.getText().toString());

                double distance = calculateDistance(adminLat, adminLon, userLat, userLon);

                // Show popup dialog with the calculated distance
                showDistanceDialog(distance);

            }
        });
        call1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumber != null) {
                    String phoneNumber = contactNum.getText().toString();
                    callContactNumber(phoneNumber);
                } else {
                    Toast.makeText(Adminuserlocation.this, "Waiting for Admins Response to get their Contact Number", Toast.LENGTH_SHORT).show();
                }

            }
        });

        DocumentReference adminActionRef = db.collection("admin_actions").document("action");
        adminActionRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Boolean isAdminActionDone = snapshot.getBoolean("done");
                    if (isAdminActionDone != null && isAdminActionDone) {
                        // Admin action is done, proceed with user screen synchronization
                        db.collection("firedeptuser")
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            // Retrieve relevant data from the user's document
                                            String Department = documentSnapshot.getString("Department");
                                            String Address = documentSnapshot.getString("Admin Address");
                                            Double latitude = documentSnapshot.getDouble("Latitude");
                                            Double longitude = documentSnapshot.getDouble("Longitude");
                                            String contactNum = documentSnapshot.getString("Contact Number");

                                            // Save the data to admin response
                                            Map<String, Object> historyData = new HashMap<>();
                                            historyData.put("Admin Address", Address);
                                            historyData.put("Department", Department);
                                            historyData.put("Latitude", latitude);
                                            historyData.put("Longitude", longitude);
                                            historyData.put("Contact Number", contactNum);


                                            // Add document data to "adminresponse" collection
                                            db.collection("adminresponse")
                                                    .add(historyData)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            // Delete documents from "firedeptuser" collection
                                                            Log.d(TAG, "Document added to collection 'adminresponse' with ID: " + documentReference.getId());
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e(TAG, "Error adding document to collection 'adminresponse': " + e.getMessage());
                                                        }
                                                    });
                                            break;
                                        }
                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            db.collection("firedeptuser")
                                                    .document(documentSnapshot.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // Document deleted successfully
                                                            Log.d(TAG, "Document deleted successfully from 'firedeptuser'");


                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Handle failure
                                                            Log.e(TAG, "Error deleting document from 'firedeptuser': " + e.getMessage());
                                                        }
                                                    });
                                            // Assuming you only want to delete one document
                                            break;
                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle failure
                                        Log.e(TAG, "Error getting documents from 'firedeptuser': " + e.getMessage());
                                    }
                                });
                        // Update user's screen by redirecting to DashboardUser
                        db.collection("admin_actions").document("action").update("done", false);
                        startActivity(new Intent(Adminuserlocation.this, DashboardUser.class));
                        finish();
                        // if (getIntent().getBooleanExtra("admin_triggered", false)) {
                        //    startActivity(new Intent(Adminuserlocation.this, DashboardUser.class));
                        //   finish();
                    }

                }
                // }
            }

        });
    }


    private void showDistanceDialog(double distance) {
        // Create and configure the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Adminuserlocation.this);
        builder.setTitle("Distance");
        builder.setMessage("The distance between admin and user locations is " + distance + " kilometers.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
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

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
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

    // Override onBackPressed method to disable the back button functionality
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Do nothing (disable back button)
        // Alternatively, you can show a toast message indicating the back button is disabled
        Toast.makeText(this, "Back button disabled in this screen", Toast.LENGTH_SHORT).show();
    }
}