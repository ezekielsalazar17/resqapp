package com.example.resqapp;

import static com.example.resqapp.AdminRegister.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardFireDepartment extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private ImageButton profileButton;
    private ImageButton imageButton; // Define ImageButton here
    private String name;
    private String address;
    private double longitude;
    private double latitude;
    private String addressadmin;
    private double longitudeadmin;
    private double latitudeadmin;
    private long contactNum;

    private String capitalizedText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboardfiredepartment);
        getSupportActionBar().hide();

        // Initialize Firebase instances
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Initialize views
        profileButton = findViewById(R.id.adminprofile);
        imageButton = findViewById(R.id.accept_button); // Initialize ImageButton

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firestore instance
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get current user ID
        userID = fAuth.getCurrentUser().getUid();

        // Initialize RecyclerView adapter with an empty list
        MyAdapter adapter = new MyAdapter(DashboardFireDepartment.this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Fetch data from Firestore "History" collection
        db.collection("History")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Item> userHistoryList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Retrieve data from Firestore document for History collection
                                String firstName = document.getString("firstName");
                                String lastName = document.getString("lastName");
                                String address = document.getString("address");
                                double latitude = document.getDouble("latitude");
                                double longitude = document.getDouble("longitude");
                                String contactNum = document.getString("contactNum");

                                Item item = new Item(firstName, lastName, address, latitude, longitude, contactNum);
                                userHistoryList.add(item);
                            }

                            // Update the RecyclerView adapter with the data from History collection
                            adapter.getItems().addAll(userHistoryList);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Error getting documents from History collection: ", task.getException());
                        }
                    }
                });

        // Fetch data from Firestore "admins" collection
        db.collection("admins")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Item> adminsList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Retrieve data from Firestore document for admins collection
                                String adminAddress = document.getString("Admin Address");
                                Double adminLatitudeObj = document.getDouble("Latitude");
                                Double adminLongitudeObj = document.getDouble("Longitude");

                                double adminLatitude = 0.0;
                                double adminLongitude = 0.0;

                                // Check if latitude and longitude values are not null
                                if (adminLatitudeObj != null && adminLongitudeObj != null) {
                                    adminLatitude = adminLatitudeObj;
                                    adminLongitude = adminLongitudeObj;
                                }

                                Item adminItem = new Item("", "", adminAddress, adminLatitude, adminLongitude, "");
                                adminsList.add(adminItem);
                            }

                            // Append the data from admins collection to the RecyclerView adapter
                            adapter.getItems().addAll(adminsList);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Error getting documents from admins collection: ", task.getException());
                        }
                    }
                });

        // Set OnClickListener for the profile button
        profileButton.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), Fireprofile.class));
        });
    }

    // Method to capitalize every word in a string
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
}
