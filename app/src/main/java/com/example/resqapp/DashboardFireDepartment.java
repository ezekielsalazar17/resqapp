package com.example.resqapp;

import static com.example.resqapp.AdminRegister.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
        profileButton = findViewById(R.id.adminprofilefire);
        imageButton = findViewById(R.id.accept_button); // Initialize ImageButton

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize empty list of items
        List<Item> items = new ArrayList<>();

        profileButton.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), Fireprofile.class));
        });

        // Initialize Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String historyCollection = "pendingfiredept";

        // Get current user ID
        userID = fAuth.getCurrentUser().getUid();

        // Fetch data from Firestore
        db.collection(historyCollection)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<Item> userHistoryList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : snapshots) {
                            // Retrieve data from Firestore document
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            firstName = capitalizeEveryWord(firstName);
                            lastName = capitalizeEveryWord(lastName);

                            String address = document.getString("address");
                            address = capitalizeEveryWord(address); // Capitalize the address

                            Double latitudeObj = document.getDouble("latitude");
                            Double longitudeObj = document.getDouble("longitude");
                            String contactNumObj = document.getString("contactNum");

                            String contactNum = contactNumObj != null ? String.valueOf(contactNumObj) : "0";
                            double latitude = latitudeObj != null ? latitudeObj.doubleValue() : 0.0;
                            double longitude = longitudeObj != null ? longitudeObj.doubleValue() : 0.0;

                            Item item = new Item(firstName, lastName, address, latitude, longitude, contactNum);
                            userHistoryList.add(item);
                        }

                        // Update RecyclerView adapter with the new data
                        MyAdapter adapter = new MyAdapter(DashboardFireDepartment.this, userHistoryList);
                        recyclerView.setAdapter(adapter);
                    }
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
