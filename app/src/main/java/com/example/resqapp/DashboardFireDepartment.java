package com.example.resqapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
    String userId;
    ImageButton profileButton;
    ImageButton imageButton;
    String name;
    String address;
    double longitude;
    double latitude;
    long contactNum;


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

        fStore.collection("users") // Replace "your_collection_name" with your actual collection name
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Intent intent = getIntent();
                                if (intent != null) {
                                    name = intent.getStringExtra("First Name");
                                    address = intent.getStringExtra("Address");
                                    longitude = intent.getDoubleExtra("Longitude", 0.0); // 0.0 is the default value if the key is not found
                                    latitude = intent.getDoubleExtra("Latitude", 0.0); // 0.0 is the default value if the key is not found
                                    contactNum = intent.getLongExtra("Contact Number", 0); // 0 is the default value if the key is not found
                                }
                                /*String address = document.getString("address");
                                double latitude = document.getDouble("latitude");
                                double longitude = document.getDouble("longitude");
                                long contactNum = document.getLong("contactNum"); // Assuming "contactNum" is stored as a long*/
                                imageButton.setImageResource(R.drawable.baseline_check_24);

                                // Create Item object and add it to the list
                                items.add(new Item(name, address, longitude, latitude, contactNum));
                            }
                            // Set adapter after fetching data
                            recyclerView.setAdapter(new MyAdapter(getApplicationContext(), items));
                        } else {
                            // Handle errors
                        }
                    }
                });

        profileButton.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), Fireprofile.class));
        });
    }
}
