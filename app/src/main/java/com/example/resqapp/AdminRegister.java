package com.example.resqapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminRegister extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final String TAG = "TAG";
    EditText Email, Password, Conpass, Number;

    TextView latitude, longitude, address;

    Button RegisterBtn;

    FirebaseAuth fAuth;

    ProgressBar progressBar;

    FirebaseFirestore firestore;

    String userID;
    private Spinner spinner;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.admin_register);

        Email = findViewById(R.id.adminemail1);
        Password = findViewById(R.id.adminpass1);
        Number = findViewById(R.id.admincontact2);
        Conpass = findViewById(R.id.adminconpass1);
        RegisterBtn = findViewById(R.id.register);

        latitude = findViewById(R.id.latitudeadmin1);
        longitude = findViewById(R.id.longitudeadmin1);
        address = findViewById(R.id.addressadmin1);

        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);
        spinner = findViewById(R.id.dropdown_menu1);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.department1, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Email.getText().toString().trim();
                String password = Password.getText().toString().trim();
                String number = Number.getText().toString().trim();
                String department = spinner.getSelectedItem().toString().trim();
                String lat = " ";
                String longi = " ";
                String add = " ";

                if(TextUtils.isEmpty(email)){
                    Email.setError("Email is Required ");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    Password.setError("Password is Required ");
                    return;
                }

                if(password.length() < 10){
                    Password.setError("Password must be Less than or Equal to 10");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(AdminRegister.this, "Admin Created", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = firestore.collection("admins").document(userID);
                            Map<String, Object> user = new HashMap<>();

                            user.put("Email", email);
                            user.put("Password", password);
                            user.put("Contact Number", number);
                            user.put("Department", department);
                            user.put("Latitude", lat);
                            user.put("Longitude", longi);
                            user.put("Admin Address", add);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "onSuccess: user Profile is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), AdminLogin.class));
                        }else{
                            Toast.makeText(AdminRegister.this, "Error !" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });


            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String choice = adapterView.getItemAtPosition(i).toString();
        Toast.makeText(getApplicationContext(), choice, Toast.LENGTH_LONG);
        Toast.makeText(this, "Make sure to choose the right department", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}