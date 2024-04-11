package com.example.resqapp;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class LocationSharingAdminPolice extends AppCompatActivity {

    private FirebaseAuth fAuth;

    public Button done1;
    private PopupWindow popupWindow;
    private long TimeBack;

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

        String userEmail = getIntent().getStringExtra("UserEmail");
        adminlat.setText(adminlat1);


        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LocationSharingAdminPolice.this, AdminDirections.class);

                intent.putExtra("user_location", userloc);
                intent.putExtra("user_latitude", (userloclat));
                intent.putExtra("user_longitude", (userloclongi));

                intent.putExtra("admin_location", adminloc);
                intent.putExtra("admin_latitude", adminlat1);
                intent.putExtra("admin_longitude", adminlongi1);

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

                        db.collection("inprogressPolice")
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
                                            db.collection("policedeptHistory")
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
                                            db.collection("inprogressPolice")
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

                        db1.collection("policedeptuser")
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
                                            db1.collection("adminresponsepolice")
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
                                            db.collection("policedeptuser")
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
                        startActivity(new Intent(LocationSharingAdminPolice.this, DashboardPoliceDepartment.class));
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
};