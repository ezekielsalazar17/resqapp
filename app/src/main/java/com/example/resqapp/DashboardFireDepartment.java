package com.example.resqapp;

import static com.example.resqapp.UserRegister.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

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

    TextView email;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private String userID;
    ImageButton profileButton;
    ImageButton imageButton;
    String name;
    String address;
    double longitude;
    double latitude;
    long contactNum;
    String capitalizedText;


 // Replace "your_image_resource" with the name of your image resource file in the drawable folder

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboardfiredepartment);
        getSupportActionBar().hide();


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        profileButton = findViewById(R.id.adminprofile);
        imageButton = findViewById(R.id.accept_button);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Item> items = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        String historyCollection = "History";

        userID = fAuth.getCurrentUser().getUid();

        db.collection(historyCollection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Item> userHistoryList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Retrieve first name and last name from document
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

                            // Initialize RecyclerView adapter with the correct context
                            MyAdapter adapter = new MyAdapter(getApplicationContext(), userHistoryList);

                            // Set the adapter to RecyclerView
                            recyclerView.setAdapter(adapter);


                            ImageButton adapterImageButton = adapter.getImageButton();

                            if (adapterImageButton == null) {
                                imageButton.setOnClickListener(v -> {
                                    Intent intent = new Intent(DashboardFireDepartment.this, LocationSharingAdmin.class);
                                    intent.putExtra("Address", address);
                                    startActivity(intent);
                                });
                            } else {
                                Log.e(TAG, "imageButton is null");
                            }



                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        profileButton.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), Fireprofile.class));
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
}
