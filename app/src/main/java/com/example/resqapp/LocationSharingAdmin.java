package com.example.resqapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationSharingAdmin extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private FirebaseFirestore firestore;
    private static final int LOCATION_PERMISSION_CODE = 101;

    Handler handler;
    long refreshTime = 5000;
    Runnable runnable;
    private long TimeBack;

    public Button done1;
    private PopupWindow popupWindow;

    TextView userLocation, adminLocation, userlat, userlong, adminlat, adminlong, userEmail;
    Button direction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_sharing_admin);

        getSupportActionBar().hide();

        userLocation = findViewById(R.id.user_loc);
        userlat = findViewById(R.id.userlatitudes);
        userlong = findViewById(R.id.userlongitudes);

        adminLocation = findViewById(R.id.admin_loc);
        adminlat = findViewById(R.id.adminlatitudes);
        adminlong = findViewById(R.id.adminlongitudes);

        direction = findViewById(R.id.go_to_gmaps);
        done1 = findViewById(R.id.done);

        userEmail = findViewById(R.id.emailuser1);

        String userloc = getIntent().getStringExtra("Address");
        userLocation.setText(userloc);
        double userloclat = getIntent().getDoubleExtra("Latitude", 0.0);
        userlat.setText(String.valueOf(userloclat));
        double userloclongi = getIntent().getDoubleExtra("Longitude", 0.0);
        userlong.setText(String.valueOf(userloclongi));

        String adminloc = getIntent().getStringExtra("Address Admin");
        adminLocation.setText(adminloc);
        String adminlat1 = getIntent().getStringExtra("Latitude Admin");
        String adminlongi1 = getIntent().getStringExtra("Longitude Admin");
        adminlat.setText(adminlat1);
        adminlong.setText(adminlongi1);

        handler = new Handler();
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(runnable, refreshTime);
                isLocationPermissionGranted();
                showLocationadmin();
            }
        }, refreshTime);

        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LocationSharingAdmin.this, AdminDirections.class);

                intent.putExtra("user_location", userloc);
                intent.putExtra("user_latitude", (userloclat));
                intent.putExtra("user_longitude", (userloclongi));

                intent.putExtra("admin_location", adminloc);
                intent.putExtra("admin_latitude", adminlat1);
                intent.putExtra("admin_longitude", adminlongi1);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });

        done1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create a LayoutInflater object
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_window, null);

                fAuth = FirebaseAuth.getInstance();

                // Create a PopupWindow object
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;
                PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // Set content for PopupWindow
                Button yesButton = popupView.findViewById(R.id.yes_button);
                Button noButton = popupView.findViewById(R.id.no_button);

                // Set onClickListener for Yes button
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("inprogressFire")
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            String firstName = documentSnapshot.getString("firstName");
                                            String lastName = documentSnapshot.getString("lastName");
                                            String address = documentSnapshot.getString("address");
                                            Double latitude = documentSnapshot.getDouble("latitude");
                                            Double longitude = documentSnapshot.getDouble("longitude");
                                            String contactNum = documentSnapshot.getString("contactNum");
                                            String userEmail = documentSnapshot.getString("useremail");

                                            // Get document data
                                            Map<String, Object> data = documentSnapshot.getData();
                                            data.put("firstName", firstName);
                                            data.put("lastName", lastName);
                                            data.put("address", address);
                                            data.put("latitude", latitude);
                                            data.put("longitude", longitude);
                                            data.put("contactNum", contactNum);
                                            data.put("useremail", userEmail);


                                            // Add document data to "firedeptHistory" collection
                                            db.collection("firedeptHistory")
                                                    .add(data)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            // Document added successfully
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Handle failure
                                                        }
                                                    });

                                            // Delete document from "inprogressFire" collection
                                            db.collection("inprogressFire")
                                                    .document(documentSnapshot.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Handle failure
                                                        }
                                                    });
                                            break;
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle failure
                                    }
                                });
                        FirebaseFirestore db1 = FirebaseFirestore.getInstance();

                        db1.collection("firedeptuser")
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            String firstName = documentSnapshot.getString("firstName");
                                            String lastName = documentSnapshot.getString("lastName");
                                            String address = documentSnapshot.getString("address");
                                            Double latitude = documentSnapshot.getDouble("latitude");
                                            Double longitude = documentSnapshot.getDouble("longitude");
                                            String contactNum = documentSnapshot.getString("contactNum");
                                            String userEmail = documentSnapshot.getString("useremail");

                                            // Get document data
                                            Map<String, Object> data = documentSnapshot.getData();
                                            data.put("firstName", firstName);
                                            data.put("lastName", lastName);
                                            data.put("address", address);
                                            data.put("latitude", latitude);
                                            data.put("longitude", longitude);
                                            data.put("contactNum", contactNum);
                                            data.put("useremail", userEmail);


                                            // Add document data to "firedeptHistory" collection
                                            db1.collection("adminresponse")
                                                    .add(data)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            // Document added successfully
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Handle failure
                                                        }
                                                    });

                                            // Delete document from "inprogressFire" collection
                                            db.collection("firedeptuser")
                                                    .document(documentSnapshot.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Handle failure
                                                        }
                                                    });
                                            break;
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle failure
                                    }
                                });
                        startActivity(new Intent(LocationSharingAdmin.this, DashboardFireDepartment.class));
                        finish();
                    }
                });

                // Set onClickListener for No button
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss(); // Dismiss the popup window
                    }
                });

                // Show the popup window at the calculated position
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
        });


    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public void showLocationadmin() {
        if (isLocationPermissionGranted()) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {

                                double latitudeadmin = location.getLatitude();
                                double longitudeadmin = location.getLongitude();

                                adminlat.setText("Latitude: " + latitudeadmin);
                                adminlong.setText("Longitude: " + longitudeadmin);

                                getAddressFromLocation(latitudeadmin, longitudeadmin);


                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();

                                    firestore = FirebaseFirestore.getInstance();

                                    firestore.collection("admins").document(userId)
                                            .update("Latitude", latitudeadmin, "Longitude", longitudeadmin, "Admin Address", getAddressFromLocation(latitudeadmin, longitudeadmin))
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
                                    firestore = FirebaseFirestore.getInstance();

                                    firestore.collection("firedeptuser").document(userId)
                                            .update("Latitude", latitudeadmin, "Longitude", longitudeadmin, "Admin Address", getAddressFromLocation(latitudeadmin, longitudeadmin))
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
            ActivityCompat.requestPermissions(LocationSharingAdmin.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

/*@SuppressLint("MissingPermission")
    public void showLocationadmin() {
        if (isLocationPermissionGranted()) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitudeadmin = location.getLatitude();
                                double longitudeadmin = location.getLongitude();

                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();
                                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                                    // Update document in admins collection
                                    firestore.collection("admins").document(userId)
                                            .update("Latitude", latitudeadmin, "Longitude", longitudeadmin, "Admin Address", getAddressFromLocation(latitudeadmin, longitudeadmin))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("Firebase", "Admin document updated successfully");

                                                    // Check if document exists in firedeptuser collection
                                                    firestore.collection("firedeptuser").document(userId)
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                    if (documentSnapshot.exists()) {
                                                                        // Update existing document
                                                                        firestore.collection("firedeptuser").document(userId)
                                                                                .update("Latitude", latitudeadmin, "Longitude", longitudeadmin, "Admin Address", getAddressFromLocation(latitudeadmin, longitudeadmin))
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Log.i("Firebase", "Firedeptuser document updated successfully");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.e("Firebase", "Error updating firedeptuser document", e);
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        // Document doesn't exist, create new document
                                                                        Map<String, Object> userLocation = new HashMap<>();
                                                                        userLocation.put("Latitude", latitudeadmin);
                                                                        userLocation.put("Longitude", longitudeadmin);
                                                                        userLocation.put("Admin Address", getAddressFromLocation(latitudeadmin, longitudeadmin));

                                                                        firestore.collection("firedeptuser").document(userId)
                                                                                .set(userLocation)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Log.i("Firebase", "New Firedeptuser document created successfully");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.e("Firebase", "Error creating new firedeptuser document", e);
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("Firebase", "Error checking firedeptuser document existence", e);
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("Firebase", "Error updating admin document", e);
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
            ActivityCompat.requestPermissions(LocationSharingAdmin.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }*/


    private Object getAddressFromLocation(double latitudeadmin, double longitudeadmin) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitudeadmin, longitudeadmin, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                adminLocation.setText(addressLine);
                return addressLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void deleteLastTransaction() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss(); // Dismiss the last dialog if it's still showing
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
    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - TimeBack > 1000){
            TimeBack = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "Press Again to Exit", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }
}