package com.example.resqapp;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboardfiredepartment);
        getSupportActionBar().hide();

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        profileButton = findViewById(R.id.adminprofile);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Item> items = new ArrayList<>();

        fStore.collection("your_collection_name") // Replace "your_collection_name" with your actual collection name
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                String address = document.getString("address");
                                double latitude = document.getDouble("latitude");
                                double longitude = document.getDouble("longitude");
                                long contactNum = document.getLong("contactNum"); // Assuming "contactNum" is stored as a long

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
