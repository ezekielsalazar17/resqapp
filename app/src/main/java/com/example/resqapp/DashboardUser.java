package com.example.resqapp;

import static com.example.resqapp.UserRegister.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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


                                    db.collection("History")
                                            .add(historyData)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d(TAG, "Document added to collection 'History' with ID: " + documentReference.getId());
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
