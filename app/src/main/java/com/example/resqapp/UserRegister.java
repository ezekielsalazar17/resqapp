package com.example.resqapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

public class UserRegister extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    public static final String TAG = "TAG";
    EditText Email, Number, Password, Conpass, Fname, Lname, Bday;

    Button RegisterBtn;

    ImageButton Id;

    FirebaseAuth fAuth;

    ProgressBar progressBar;

    FirebaseFirestore firestore;
    String userID;

    private Spinner spinner;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.user_register);

        Email = findViewById(R.id.useremail1);
        Number = findViewById(R.id.contactuser2);
        Password = findViewById(R.id.userpass1);
        Conpass = findViewById(R.id.userconpass1);
        RegisterBtn = findViewById(R.id.register);
        Fname = findViewById(R.id.fname1);
        Lname = findViewById(R.id.lname1);
        Bday = findViewById(R.id.birthday1);
        spinner = findViewById(R.id.dropdown_menu1);

        Id = findViewById(R.id.ocrid);

        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        String fname = getIntent().getStringExtra("charactersAfterSpecial");
        Fname.setText(fname);

        String lname = getIntent().getStringExtra("extractedCharacters");
        Lname.setText(lname);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dropdown_menu1, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), DashboardUser.class));
            finish();
        }

        Id.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), Ocr.class));
        });

        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Email.getText().toString().trim();
                String password = Password.getText().toString().trim();
                String number = Number.getText().toString().trim();
                String fname = Fname.getText().toString().trim();
                String lname = Lname.getText().toString().trim();
                String bday = Bday.getText().toString().trim();
                String latitude = " ";
                String longitude = " ";
                String idnum = " ";
                String id = spinner.getSelectedItem().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(number)) {
                    Toast.makeText(UserRegister.this, "Email, password, and contact number are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 10) {
                    Toast.makeText(UserRegister.this, "Password must be at least 10 characters long", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = firestore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("Email", email);
                            user.put("First Name", fname);
                            user.put("Last Name", lname);
                            user.put("Password", password);
                            user.put("Contact Number", number);
                            user.put("Latitude", latitude);
                            user.put("Longitude", longitude);
                            user.put("Birthday", bday);
                            user.put("ID", id);
                            user.put("ID Number", getIntent().getStringExtra("charactersFromNineteenthLine"));
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "User profile created for " + userID);
                                    Toast.makeText(UserRegister.this, "User created successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), UserLogin.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error creating user profile", e);
                                    Toast.makeText(UserRegister.this, "Error creating user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            Log.e(TAG, "Error creating user", task.getException());
                            Toast.makeText(UserRegister.this, "Error creating user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
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