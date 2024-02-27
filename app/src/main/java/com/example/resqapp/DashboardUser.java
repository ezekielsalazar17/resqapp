package com.example.resqapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardUser extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    private static final int LOCATION_PERMISSION_CODE = 101;
    Button locationSharing;
    ImageButton firebutton, profilebutton;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;


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
        profilebutton = findViewById(R.id.profile_button);

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

        // Set OnClickListener for firebutton
        firebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProgressDialog progressDialog = new ProgressDialog(DashboardUser.this);
                progressDialog.setMessage("Sending...");
                progressDialog.setCancelable(true);
                progressDialog.show();

                // Initialize FirebaseAuth instance
                FirebaseAuth fAuth = FirebaseAuth.getInstance();
                FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                // Get the currently signed-in user
                FirebaseUser user = fAuth.getCurrentUser();
                if (user != null) {
                    String userID = user.getUid();
                    fetchUserData(userID, progressDialog); // Call to fetchUserData method
                } else {
                    progressDialog.dismiss(); // Dismiss dialog if user is not authenticated
                    // Redirect user to login screen or handle as per your app's logic
                    Toast.makeText(DashboardUser.this, "User not authenticated. Redirecting to login screen...", Toast.LENGTH_SHORT).show();
                    // Example: startActivity(new Intent(DashboardUser.this, LoginActivity.class));
                }
            }

            // Define fetchUserData method inside OnClickListener
            private void fetchUserData(String userID, ProgressDialog progressDialog) {
                FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                DocumentReference documentReference = fStore.collection("users").document(userID);

                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                // Get user data from the snapshot
                                String firstname = snapshot.getString("First Name");
                                String address = snapshot.getString("Address");
                                Double longitude = snapshot.getDouble("Longitude");
                                Double latitude = snapshot.getDouble("Latitude");
                                String contactNum = snapshot.getString("Contact Number");

                                // Check if any required field is null
                                if (firstname != null && address != null && longitude != null && latitude != null && contactNum != null) {
                                    // Create an intent
                                    Intent intent = new Intent(this, DashboardUser.class);

                                    // Pass data to the intent
                                    intent.putExtra("First Name", firstname);
                                    intent.putExtra("Address", address);
                                    intent.putExtra("Longitude", longitude);
                                    intent.putExtra("Latitude", latitude);
                                    intent.putExtra("Contact Number", contactNum);

                                    // Start the new activity
                                    startActivity(intent);
                                } else {
                                    // Handle null fields
                                    Toast.makeText(DashboardUser.this, "User data is incomplete", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Handle the case where the snapshot doesn't exist
                                Toast.makeText(DashboardUser.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle errors
                            Log.e("Firebase", "Error getting user data", task.getException());
                            Toast.makeText(DashboardUser.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        }
                        // Dismiss the progress dialog
                        progressDialog.dismiss();
                    }
                });
            }

        });
    }


    private void OnGPS() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_CODE);
    }
}
