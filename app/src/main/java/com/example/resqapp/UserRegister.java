package com.example.resqapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserRegister extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    public static final String TAG = "TAG";
    EditText Email, Number, Password, Conpass, Fname, Lname, Bday;
    Button RegisterBtn;
    ImageView calendarDate, passvis, conpassvis;
    Calendar calendar;
    ImageButton Id;
    int year, month, day;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore firestore;
    String userID;
    DatePickerDialog datePickerDialog;


    private Spinner spinner;
    private Context activity;

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
        calendarDate = findViewById(R.id.calendar);
        passvis = findViewById(R.id.password_visible);
        conpassvis = findViewById(R.id.conpass_visible);

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
                String conpass = Conpass.getText().toString().trim();
                String latitude = " ";
                String longitude = " ";
                String idnum = " ";
                String id = spinner.getSelectedItem().toString().trim();
                String typeofacc = "User";


                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(number)) {
                    Toast.makeText(UserRegister.this, "Email, password, and contact number are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 10) {
                    Toast.makeText(UserRegister.this, "Password must be at least 10 characters long", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(conpass)){
                    Toast.makeText(UserRegister.this, "Your password is not match to Confirm Password", Toast.LENGTH_SHORT).show();
                    return;
                }


                progressBar.setVisibility(View.VISIBLE); // Show progress bar while processing

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Send email verification
                            fAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> emailTask) {
                                            if (emailTask.isSuccessful()) {
                                                Log.d(TAG, "Email verification sent.");
                                                Toast.makeText(UserRegister.this, "Email verification sent. Please check your email.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.e(TAG, "Error sending email verification", emailTask.getException());
                                                Toast.makeText(UserRegister.this, "Error sending email verification: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            // Continue with user data storage
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
                            user.put("Type of Account", typeofacc);
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

        passvis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectionStart = Password.getSelectionStart();
                int selectionEnd = Password.getSelectionEnd();

                if (Password.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    Password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }

                // Preserve cursor position
                Password.setSelection(selectionStart, selectionEnd);
            }
        });

        conpassvis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectionStart = Conpass.getSelectionStart();
                int selectionEnd = Conpass.getSelectionEnd();

                if (Conpass.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    Conpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    Conpass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }

                // Preserve cursor position
                Conpass.setSelection(selectionStart, selectionEnd);
            }
        });

        calendarDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(UserRegister.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        // Increment month by 1 since DatePickerDialog months are zero-based
                        month++;
                        Bday.setText(year + "/" + month + "/" + dayOfMonth);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String choice = adapterView.getItemAtPosition(i).toString();
        Toast.makeText(getApplicationContext(), choice, Toast.LENGTH_LONG);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
