package com.example.resqapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
public class Fireprofile extends AppCompatActivity {
    Button Logout;
    TextView phone, idNum, email, department;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fStore;
    String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_fire_admin);

        phone = findViewById(R.id.fire_contact_number);
        idNum = findViewById(R.id.fire_id_num1);
        email = findViewById(R.id.fire_email1);
        department = findViewById(R.id.fire_department1);

        firebaseAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


        if (firebaseAuth.getCurrentUser() != null) {
            userId = firebaseAuth.getCurrentUser().getUid();

            // Reference to the admin's document
            DocumentReference documentReference = fStore.collection("admins").document(userId);

            // Add the snapshot listener
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.e("Firestore", "Error getting data: " + error.getMessage());
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Data retrieved, update UI
                        phone.setText(documentSnapshot.getString("Contact Number"));
                        email.setText(documentSnapshot.getString("Email"));
                        department.setText(documentSnapshot.getString("Department"));
                    } else {
                        Log.d("Firestore", "Document does not exist");
                    }
                }
            });
        }

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();*/

        Logout = findViewById(R.id.sign_out);

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the sign_out button is clicked
                if (v.getId() == R.id.sign_out) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), UserOrAdminLogin.class));
                    finish();
                }
            }
        });

        }
    }

