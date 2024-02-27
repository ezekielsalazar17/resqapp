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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardUser extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    private static final int LOCATION_PERMISSION_CODE = 101;
    Button locationSharing;
    ImageButton firebutton, profilebutton;
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

        OnGPS();
        requestLocationPermission();

        firebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProgressDialog progressDialog = new ProgressDialog(DashboardUser.this);
                progressDialog.setMessage("Sending...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Initialize FirebaseAuth instance
                FirebaseAuth fAuth = FirebaseAuth.getInstance();

                // Get the currently signed-in user
                FirebaseUser user = fAuth.getCurrentUser();
                if (user != null) {
                    String userID = user.getUid();
                    fetchUserData(userID, progressDialog);
                } else {
                    // Redirect user to login screen or handle as per your app's logic
                    Toast.makeText(DashboardUser.this, "User not authenticated. Redirecting to login screen...", Toast.LENGTH_SHORT).show();
                    // Example: startActivity(new Intent(DashboardUser.this, LoginActivity.class));
                }
            }


        private void fetchUserData(String userID, ProgressDialog progressDialog) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Check if the snapshot exists and has children
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        // Get the first name from the snapshot
                        String firstname = snapshot.child("First Name").getValue(String.class);

                        // Create an intent
                        Intent intent = new Intent(DashboardUser.this, DashboardFireDepartment.class);

                        // Pass data to the intent
                        intent.putExtra("First Name", firstname);

                        // Start the new activity
                        startActivity(intent);

                        // Dismiss the progress dialog
                        progressDialog.dismiss();
                    } else {
                        // Handle the case where the snapshot doesn't exist or has no children
                        progressDialog.dismiss();
                        // Show an error message or handle it as per your requirement
                        Toast.makeText(DashboardUser.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    // Handle onCancelled event
                    Log.e("Firebase", "onCancelled", error.toException());
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
