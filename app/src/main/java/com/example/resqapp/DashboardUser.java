package com.example.resqapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;


public class DashboardUser extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    private static final int LOCATION_PERMISSION_CODE = 101;
    private static final int REQUEST_CODE = 111;
    private static String TAG;
    Button locationSharing;
    ImageButton firebutton, policebutton, coastbutton, ambulancebutton;
    ImageView profilebutton;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;
    ImageView pictureuser;
    private String userID;
    private String addadmin;
    private String latadmin;
    private String longiadmin;
    private MyAdapter.LocationFetchListener locationFetchListener;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

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



        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.




        // Set OnClickListener for firebutton
        firebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isFinishing() && !isDestroyed()) {

                    BiometricManager biometricManager = BiometricManager.from(DashboardUser.this);
                    switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                        case BiometricManager.BIOMETRIC_SUCCESS:
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            Toast.makeText(DashboardUser.this, "Biometric sensor not available", Toast.LENGTH_SHORT).show();
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            Toast.makeText(DashboardUser.this, "Biometric sensor is busy", Toast.LENGTH_SHORT).show();
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                            enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
                            startActivityForResult(enrollIntent, REQUEST_CODE);
                            break;
                    }

                    executor = ContextCompat.getMainExecutor(DashboardUser.this);
                    biometricPrompt = new BiometricPrompt(DashboardUser.this,
                            executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(getApplicationContext(),
                                            "Authentication error: " + errString, Toast.LENGTH_SHORT)
                                    .show();
                            deleteLastTransaction();
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Toast.makeText(getApplicationContext(), "Biometric Authentication succeeded!", Toast.LENGTH_SHORT).show();
                            deleteLastTransaction();
                            getCurrentLocation();
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


                                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                String time = dateFormat.format(new Date());

                                                // Create a new document in the "History" collection with user's first name and last name
                                                Map<String, Object> historyData = new HashMap<>();
                                                historyData.put("firstName", firstName);
                                                historyData.put("lastName", lastName);
                                                historyData.put("address", address);
                                                historyData.put("latitude", latitude);
                                                historyData.put("longitude", longitude);
                                                historyData.put("contactNum", contactNum);
                                                historyData.put("useremail", email);
                                                historyData.put("timestamp", time);

                                                db1.collection("pendingfiredept")
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
                            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                            View popupView1 = inflater.inflate(R.layout.popup_window_user, null);

                            // Create a PopupWindow object
                            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            boolean focusable = true;
                            PopupWindow popupWindow = new PopupWindow(popupView1, width, height, focusable);

                            // Set content for PopupWindow
                            TextView dept = popupView1.findViewById(R.id.department_admin);
                            TextView addressadmin = popupView1.findViewById(R.id.admin_address);
                            TextView lat = popupView1.findViewById(R.id.admin_latitude);
                            TextView longi = popupView1.findViewById(R.id.admin_longitude);
                            TextView contact = popupView1.findViewById(R.id.admin_contactnum1);

                            String historyCollection = "firedeptuser";

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
                                                        // Data is fetched, proceed to fetch the rest of the data
                                                        String address = document.getString("Admin Address");
                                                        address = capitalizeEveryWord(address); // Capitalize the address
                                                        String contactNum1 = document.getString("Contact Number");
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
                                                        dept.setText(department);

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Intent intent = new Intent(DashboardUser.this, Adminuserlocation.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }, 2000);

                                                        popupWindow.dismiss();

                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    });
                            popupWindow.setOutsideTouchable(false); // Prevent dismissing when touching outside
                            popupWindow.setFocusable(false);
                            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                            deleteLastTransaction();

                        }
                    });

                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Biometric login for my app")
                            .setSubtitle("Log in using your biometric credential")
                            .setNegativeButtonText("Use account password")
                            .build();
                    biometricPrompt.authenticate(promptInfo);
                }
            }
        });
        policebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing() && !isDestroyed()) {
                    BiometricManager biometricManager = BiometricManager.from(DashboardUser.this);
                    switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                        case BiometricManager.BIOMETRIC_SUCCESS:
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            Toast.makeText(DashboardUser.this, "Biometric sensor not available", Toast.LENGTH_SHORT).show();
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            Toast.makeText(DashboardUser.this, "Biometric sensor is busy", Toast.LENGTH_SHORT).show();
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                            enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
                            startActivityForResult(enrollIntent, REQUEST_CODE);
                            break;
                    }

                    executor = ContextCompat.getMainExecutor(DashboardUser.this);
                    biometricPrompt = new BiometricPrompt(DashboardUser.this,
                            executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(getApplicationContext(),
                                            "Authentication error: " + errString, Toast.LENGTH_SHORT)
                                    .show();
                            deleteLastTransaction();
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Toast.makeText(getApplicationContext(), "Biometric Authentication succeeded!", Toast.LENGTH_SHORT).show();
                            deleteLastTransaction();
                            getCurrentLocation();
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


                                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                String time = dateFormat.format(new Date());

                                                // Create a new document in the "History" collection with user's first name and last name
                                                Map<String, Object> historyData = new HashMap<>();
                                                historyData.put("firstName", firstName);
                                                historyData.put("lastName", lastName);
                                                historyData.put("address", address);
                                                historyData.put("latitude", latitude);
                                                historyData.put("longitude", longitude);
                                                historyData.put("contactNum", contactNum);
                                                historyData.put("useremail", email);
                                                historyData.put("timestamp", time);

                                                db1.collection("pendingpolicedept")
                                                        .add(historyData)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                Log.d(TAG, "Document added to collection 'pendingpolicedept' with ID: " + documentReference.getId());
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
                            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                            View popupView1 = inflater.inflate(R.layout.popup_window_user, null);

                            // Create a PopupWindow object
                            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            boolean focusable = true;
                            PopupWindow popupWindow = new PopupWindow(popupView1, width, height, focusable);

                            // Set content for PopupWindow
                            TextView dept = popupView1.findViewById(R.id.department_admin);
                            TextView addressadmin = popupView1.findViewById(R.id.admin_address);
                            TextView lat = popupView1.findViewById(R.id.admin_latitude);
                            TextView longi = popupView1.findViewById(R.id.admin_longitude);
                            TextView contact = popupView1.findViewById(R.id.admin_contactnum1);

                            String historyCollection = "policedeptuser";

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
                                                    // Check if the user email matches the email stored in the policedeptuser collection
                                                    String userEmail = document.getString("User Email");
                                                    if (userEmail != null && userEmail.equals(fAuth.getCurrentUser().getEmail())) {
                                                        // Data is fetched, proceed to fetch the rest of the data
                                                        String address = document.getString("Admin Address");
                                                        address = capitalizeEveryWord(address); // Capitalize the address
                                                        String contactNum1 = document.getString("Contact Number");
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
                                                        dept.setText(department);

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Intent intent = new Intent(DashboardUser.this, Adminuserlocationpolice.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }, 2000);

                                                        popupWindow.dismiss();

                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    });
                            popupWindow.setOutsideTouchable(false); // Prevent dismissing when touching outside
                            popupWindow.setFocusable(false);
                            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                            deleteLastTransaction();
                        }
                    });

                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Biometric login for my app")
                            .setSubtitle("Log in using your biometric credential")
                            .setNegativeButtonText("Use account password")
                            .build();
                    biometricPrompt.authenticate(promptInfo);
                }
            }
        });
        coastbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing() && !isDestroyed()) {
                    BiometricManager biometricManager = BiometricManager.from(DashboardUser.this);
                    switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                        case BiometricManager.BIOMETRIC_SUCCESS:
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            Toast.makeText(DashboardUser.this, "Biometric sensor not available", Toast.LENGTH_SHORT).show();
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            Toast.makeText(DashboardUser.this, "Biometric sensor is busy", Toast.LENGTH_SHORT).show();
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                            enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
                            startActivityForResult(enrollIntent, REQUEST_CODE);
                            break;
                    }

                    executor = ContextCompat.getMainExecutor(DashboardUser.this);
                    biometricPrompt = new BiometricPrompt(DashboardUser.this,
                            executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(getApplicationContext(),
                                            "Authentication error: " + errString, Toast.LENGTH_SHORT)
                                    .show();
                            deleteLastTransaction();
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Toast.makeText(getApplicationContext(), "Biometric Authentication succeeded!", Toast.LENGTH_SHORT).show();
                            deleteLastTransaction();
                            getCurrentLocation();
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


                                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                String time = dateFormat.format(new Date());

                                                // Create a new document in the "History" collection with user's first name and last name
                                                Map<String, Object> historyData = new HashMap<>();
                                                historyData.put("firstName", firstName);
                                                historyData.put("lastName", lastName);
                                                historyData.put("address", address);
                                                historyData.put("latitude", latitude);
                                                historyData.put("longitude", longitude);
                                                historyData.put("contactNum", contactNum);
                                                historyData.put("useremail", email);
                                                historyData.put("timestamp", time);

                                                db1.collection("pendingcoastdept")
                                                        .add(historyData)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                Log.d(TAG, "Document added to collection 'pendingcoastdept' with ID: " + documentReference.getId());
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
                            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                            View popupView1 = inflater.inflate(R.layout.popup_window_user, null);

                            // Create a PopupWindow object
                            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            boolean focusable = true;
                            PopupWindow popupWindow = new PopupWindow(popupView1, width, height, focusable);

                            // Set content for PopupWindow
                            TextView dept = popupView1.findViewById(R.id.department_admin);
                            TextView addressadmin = popupView1.findViewById(R.id.admin_address);
                            TextView lat = popupView1.findViewById(R.id.admin_latitude);
                            TextView longi = popupView1.findViewById(R.id.admin_longitude);
                            TextView contact = popupView1.findViewById(R.id.admin_contactnum1);

                            String historyCollection = "coastdeptuser";

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
                                                    // Check if the user email matches the email stored in the policedeptuser collection
                                                    String userEmail = document.getString("User Email");
                                                    if (userEmail != null && userEmail.equals(fAuth.getCurrentUser().getEmail())) {
                                                        // Data is fetched, proceed to fetch the rest of the data
                                                        String address = document.getString("Admin Address");
                                                        address = capitalizeEveryWord(address); // Capitalize the address
                                                        String contactNum1 = document.getString("Contact Number");
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
                                                        dept.setText(department);

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Intent intent = new Intent(DashboardUser.this, Adminuserlocationcoast.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }, 2000);

                                                        popupWindow.dismiss();

                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    });
                            popupWindow.setOutsideTouchable(false); // Prevent dismissing when touching outside
                            popupWindow.setFocusable(false);
                            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                            deleteLastTransaction();
                        }
                    });

                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Biometric login for my app")
                            .setSubtitle("Log in using your biometric credential")
                            .setNegativeButtonText("Use account password")
                            .build();
                    biometricPrompt.authenticate(promptInfo);
                }
            }
        });
        ambulancebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing() && !isDestroyed()) {
                    BiometricManager biometricManager = BiometricManager.from(DashboardUser.this);
                    switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                        case BiometricManager.BIOMETRIC_SUCCESS:
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            Toast.makeText(DashboardUser.this, "Biometric sensor not available", Toast.LENGTH_SHORT).show();
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            Toast.makeText(DashboardUser.this, "Biometric sensor is busy", Toast.LENGTH_SHORT).show();
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                            enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
                            startActivityForResult(enrollIntent, REQUEST_CODE);
                            break;
                    }

                    executor = ContextCompat.getMainExecutor(DashboardUser.this);
                    biometricPrompt = new BiometricPrompt(DashboardUser.this,
                            executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(getApplicationContext(),
                                            "Authentication error: " + errString, Toast.LENGTH_SHORT)
                                    .show();
                            deleteLastTransaction();
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Toast.makeText(getApplicationContext(), "Biometric Authentication succeeded!", Toast.LENGTH_SHORT).show();
                            getCurrentLocation();
                            deleteLastTransaction();
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


                                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                String time = dateFormat.format(new Date());

                                                // Create a new document in the "History" collection with user's first name and last name
                                                Map<String, Object> historyData = new HashMap<>();
                                                historyData.put("firstName", firstName);
                                                historyData.put("lastName", lastName);
                                                historyData.put("address", address);
                                                historyData.put("latitude", latitude);
                                                historyData.put("longitude", longitude);
                                                historyData.put("contactNum", contactNum);
                                                historyData.put("useremail", email);
                                                historyData.put("timestamp", time);

                                                db1.collection("pendingambulancedept")
                                                        .add(historyData)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                Log.d(TAG, "Document added to collection 'pendingambulancedept' with ID: " + documentReference.getId());
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
                            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                            View popupView1 = inflater.inflate(R.layout.popup_window_user, null);

                            // Create a PopupWindow object
                            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            boolean focusable = true;
                            PopupWindow popupWindow = new PopupWindow(popupView1, width, height, focusable);

                            // Set content for PopupWindow
                            TextView dept = popupView1.findViewById(R.id.department_admin);
                            TextView addressadmin = popupView1.findViewById(R.id.admin_address);
                            TextView lat = popupView1.findViewById(R.id.admin_latitude);
                            TextView longi = popupView1.findViewById(R.id.admin_longitude);
                            TextView contact = popupView1.findViewById(R.id.admin_contactnum1);

                            String historyCollection = "ambulancedeptuser";

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
                                                    // Check if the user email matches the email stored in the policedeptuser collection
                                                    String userEmail = document.getString("User Email");
                                                    if (userEmail != null && userEmail.equals(fAuth.getCurrentUser().getEmail())) {
                                                        // Data is fetched, proceed to fetch the rest of the data
                                                        String address = document.getString("Admin Address");
                                                        address = capitalizeEveryWord(address); // Capitalize the address
                                                        String contactNum1 = document.getString("Contact Number");
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
                                                        dept.setText(department);

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Intent intent = new Intent(DashboardUser.this, Adminuserlocationambulance.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }, 2000);

                                                        popupWindow.dismiss();

                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    });
                            popupWindow.setOutsideTouchable(false); // Prevent dismissing when touching outside
                            popupWindow.setFocusable(false);
                            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                            deleteLastTransaction();
                        }
                    });

                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Biometric login for my app")
                            .setSubtitle("Log in using your biometric credential")
                            .setNegativeButtonText("Use account password")
                            .build();
                    biometricPrompt.authenticate(promptInfo);
                }
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
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Do nothing (disable back button)
        // Alternatively, you can show a toast message indicating the back button is disabled
        Toast.makeText(this, "Back button disabled in this screen", Toast.LENGTH_SHORT).show();
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(DashboardUser.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) DashboardUser.this.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void getCurrentLocation() {
        if (isLocationPermissionGranted() && isLocationEnabled()) {
            // Use FusedLocationProviderClient to get the user's current location
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(DashboardUser.this);
            if (ActivityCompat.checkSelfPermission(DashboardUser.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DashboardUser.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(DashboardUser.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Get the latitude and longitude of the user's current location
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                // Get the address from latitude and longitude
                                getAddressFromLocation(latitude, longitude);
                                // Update Firestore with the new location
                                updateFirestoreWithLocation(latitude, longitude);
                            } else {
                                // Location is null, request location permission if not granted
                                ActivityCompat.requestPermissions(DashboardUser.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                            }
                        }
                    });
        } else {
            // Location permission not granted or location services not enabled, request permission or prompt user to enable location
            if (!isLocationPermissionGranted()) {
                ActivityCompat.requestPermissions(DashboardUser.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            } else {
                // Location permission granted, but location services not enabled
                showLocationSettingsDialog();
            }
        }
    }

    private void updateFirestoreWithLocation(double latitude, double longitude) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // Get Firestore instance

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
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardUser.this);
        builder.setMessage("Location services are disabled. Do you want to enable them?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DashboardUser.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private Object getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(DashboardUser.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                return addressLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Fetch the saved image URI from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("image_pref_user", MODE_PRIVATE);
        String savedImageUriString = preferences.getString("image_uri_user", null);
        if (savedImageUriString != null) {
            Uri savedImageUri = Uri.parse(savedImageUriString);
            // Load the saved image into ImageView
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), savedImageUri);
                // Apply circular mask to the bitmap
                Bitmap circularBitmap = getCircleBitmap(bitmap);
                profilebutton.setImageBitmap(circularBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    private Bitmap getCircleBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int diameter = Math.min(width, height);

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, (width - diameter) / 2, (height - diameter) / 2, diameter, diameter);

        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, diameter, diameter);
        final RectF rectF = new RectF(rect);
        final float roundPx = diameter / 2;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(croppedBitmap, rect, rect, paint);

        return output;
    }
    private void deleteLastTransaction() {
        if (biometricPrompt != null) {
            biometricPrompt.cancelAuthentication(); // Dismiss the fingerprint authentication dialog
        }
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