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
                                String name = firstName + lastName;

                                // Create a UserHistory object with retrieved data
                                Item userHistory = new Item(name);
                                userHistoryList.add(userHistory);
                            }

                            // Now you have a list of UserHistory objects containing first name and last name
                            // Pass this list to your RecyclerView adapter and update the UI
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        profileButton.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), Fireprofile.class));
        });
    }
}
