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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView1 = inflater.inflate(R.layout.popup_window_user, null);


                // Create a PopupWindow object
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;
                PopupWindow popupWindow = new PopupWindow(popupView1, width, height, focusable);

                // Set content for PopupWindow
                Button ok_button = popupView1.findViewById(R.id.ok_buttonuser);
                TextView dept = popupView1.findViewById(R.id.department_admin);
                TextView addressadmin = popupView1.findViewById(R.id.admin_address);
                TextView lat = popupView1.findViewById(R.id.admin_latitude);
                TextView longi = popupView1.findViewById(R.id.admin_longitude);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView contact = popupView1.findViewById(R.id.admin_contactnum1);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView fetch = popupView1.findViewById(R.id.admin_fetch);

                String historyCollection = "firedeptuser";

                userID = fAuth.getCurrentUser().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection(historyCollection)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen failed.", e);
                                    return;
                                }

                                if (snapshots != null) {
                                    for (QueryDocumentSnapshot document : snapshots) {
                                        // Check if the user email matches the email stored in the firedeptuser collection
                                        String userEmail = document.getString("User Email");
                                        if (userEmail != null && userEmail.equals(fAuth.getCurrentUser().getEmail())) {
                                            String fetched = document.getString("Fetched");
                                            if ("false".equals(fetched)) {
                                                // Data is fetched, proceed to fetch the rest of the data
                                                String address = document.getString("Admin Address");
                                                address = capitalizeEveryWord(address); // Capitalize the address
                                                String contactNum1 = document.getString("Contact Number");
                                                String fetching1 = document.getString("Fetched");
                                                String department = document.getString("Department");

                                                Double latitudeObj = document.getDouble("Latitude");
                                                Double longitudeObj = document.getDouble("Longitude");

                                                double latitude = latitudeObj != null ? latitudeObj : 0.0;
                                                double longitude = longitudeObj != null ? longitudeObj : 0.0;

                                                // Set UI elements
                                                addressadmin.setText(address);
                                                lat.setText(String.valueOf(latitude));
                                                longi.setText(String.valueOf(longitude));
                                                contact.setText(contactNum1);
                                                fetch.setText(fetching1);
                                            } else {
                                                Toast.makeText(DashboardUser.this, "There's no admin response", Toast.LENGTH_SHORT).show();
                                            }
                                            break; // Break the loop after finding the matching user email
                                        }
                                    }
                                }
                            }
                        });
                ok_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Define the collection name for fire department user data
                        String fireDeptUserCollection = "firedeptuser";

                        // Fetch data from Firestore and update the "Fetched" field
                        db.collection(fireDeptUserCollection)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                // Update the "Fetched" field
                                                document.getReference().update("Fetched", "true")
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // Update successful
                                                                Log.d(TAG, "Fetched field updated successfully.");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Handle failure
                                                                Log.e(TAG, "Error updating Fetched field: " + e.getMessage());
                                                            }
                                                        });
                                                // No need to continue the loop, assuming userID is unique
                                                break;
                                            }
                                        } else {
                                            Log.e(TAG, "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                        if(addressadmin != null) {
                            startActivity(new Intent(DashboardUser.this, Adminuserlocation.class));
                        }else{
                            Toast.makeText(DashboardUser.this, "Please wait for Admin Response", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                // Get Firebase instance
                FirebaseFirestore db1 = FirebaseFirestore.getInstance();

                // Define the collection name for user data
                String userCollection = "users";

                // Get the current user ID (assuming you have it)
                String userID = getCurrentUserID(); // Replace with your method to get the user ID

                // Retrieve user's first name and last name from the "users" collection
                db1.collection(userCollection)
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
                                    String email = documentSnapshot.getString("Email");

                                    // Create a new document in the "History" collection with user's first name and last name
                                    Map<String, Object> historyData = new HashMap<>();
                                    historyData.put("firstName", firstName);
                                    historyData.put("lastName", lastName);
                                    historyData.put("address", address);
                                    historyData.put("latitude", latitude);
                                    historyData.put("longitude", longitude);
                                    historyData.put("contactNum", contactNum);
                                    historyData.put("useremail", email);

                                    db.collection("pendingfiredept")
                                            .add(historyData)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d(TAG, "Document added to collection 'pendingfiredept' with ID: " + documentReference.getId());
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