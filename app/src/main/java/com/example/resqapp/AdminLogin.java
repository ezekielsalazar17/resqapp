package com.example.resqapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.resqapp.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLogin extends AppCompatActivity {

    EditText email, password;
    Button login;
    CheckBox rememberme, fire, ambulance, police, coast;
    TextView createText, signup, forgotpass;
    FirebaseAuth fAuth;
    ImageView imageView;
    FirebaseFirestore firestore;
    public static final String SHARED_PREFS = "sharedPrefs";

    private static final String PREFS_NAME = "MyPrefs";
    private static final String LOGIN_EXPIRY = "loginExpiry";

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.admin_login);

        email = findViewById(R.id.emailadmin);
        password = findViewById(R.id.pw2);
        login = findViewById(R.id.login_button1);
        createText = findViewById(R.id.donthaveaccount);
        signup = findViewById(R.id.signup);
        fAuth = FirebaseAuth.getInstance();
        imageView = findViewById(R.id.imageView4);
        forgotpass = findViewById(R.id.forgotpass);
        rememberme = findViewById(R.id.remember_me_checkboxadmin);
        fire = findViewById(R.id.fire_checkbox);
        ambulance = findViewById(R.id.ambulance_checkbox);
        police = findViewById(R.id.police_checkbox);
        coast = findViewById(R.id.coastguard_checkbox);

        fire.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(compoundButton.isChecked()){
                    ambulance.setChecked(false);
                    police.setChecked(false);
                    coast.setChecked(false);

                    SharedPreferences preferences = getSharedPreferences("checkboxfire", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(AdminLogin.this, "You are in Fire Department", Toast.LENGTH_SHORT).show();
                }
                else if(!compoundButton.isChecked()){
                    SharedPreferences preferences = getSharedPreferences("checkboxfire", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(AdminLogin.this, "Unchecked", Toast.LENGTH_SHORT).show();

                }
            }
        });

        ambulance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(compoundButton.isChecked()){
                    fire.setChecked(false);
                    police.setChecked(false);
                    coast.setChecked(false);

                    SharedPreferences preferences = getSharedPreferences("checkboxambulance", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(AdminLogin.this, "You are in Ambulance Department", Toast.LENGTH_SHORT).show();
                }
                else if(!compoundButton.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("checkboxambulance", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(AdminLogin.this, "Unchecked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        police.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(compoundButton.isChecked()){
                    ambulance.setChecked(false);
                    fire.setChecked(false);
                    coast.setChecked(false);

                    SharedPreferences preferences = getSharedPreferences("checkboxpolice", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(AdminLogin.this, "You are in Police Department", Toast.LENGTH_SHORT).show();
                }
                else if(!compoundButton.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("checkboxpolice", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(AdminLogin.this, "Unchecked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        coast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(compoundButton.isChecked()){
                    ambulance.setChecked(false);
                    police.setChecked(false);
                    fire.setChecked(false);

                    SharedPreferences preferences = getSharedPreferences("checkboxcoast", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(AdminLogin.this, "You are in Coast Guard Department", Toast.LENGTH_SHORT).show();
                }
                else if(!compoundButton.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("checkboxcoast", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(AdminLogin.this, "Unchecked", Toast.LENGTH_SHORT).show();
                }
            }
        });



        SharedPreferences preferences = getSharedPreferences("checkboxadmin", MODE_PRIVATE);
        SharedPreferences preferences2 = getSharedPreferences("checkboxfire", MODE_PRIVATE);
        SharedPreferences preferences3 = getSharedPreferences("checkboxambulance", MODE_PRIVATE);
        SharedPreferences preferences4 = getSharedPreferences("checkboxpolice", MODE_PRIVATE);
        SharedPreferences preferences5 = getSharedPreferences("checkboxcoast", MODE_PRIVATE);

        String checkboxadmin = preferences.getString("remember","");
        String checkboxfire = preferences2.getString("remember", "");
        String checkboxambulance = preferences3.getString("remember", "");
        String checkboxpolice = preferences4.getString("remember", "");
        String checkboxcoast = preferences5.getString("remember", "");


        if(checkboxadmin.equals("true")){
            if (checkboxfire.equals("true")){
                Intent intent = new Intent(AdminLogin.this, DashboardFireDepartment.class);
                startActivity(intent);
                finish();
            }

        } else if (checkboxadmin.equals("true")){
            if (checkboxcoast.equals("true")){
                Intent intent = new Intent(AdminLogin.this, DashboardCoastGuardDepartment.class);
                startActivity(intent);
                finish();
            }
        } else if (checkboxadmin.equals("true")) {
            if (checkboxpolice.equals("true")){
                Intent intent = new Intent(AdminLogin.this, DashboardPoliceDepartment.class);
                startActivity(intent);
                finish();
            }
        } else if (checkboxadmin.equals("true")) {
            if (checkboxambulance.equals("true")){
                Intent intent = new Intent(AdminLogin.this, DashboardAmbulanceDepartment.class);
                startActivity(intent);
                finish();
            }
        } else if(checkboxadmin.equals("false")) {
            Toast.makeText(this, "Please Sign in", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
        }

        rememberme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    SharedPreferences preferences = getSharedPreferences("checkboxadmin", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(AdminLogin.this, "Remember Me is Checked", Toast.LENGTH_SHORT).show();

                }else if(!compoundButton.isChecked()){
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(AdminLogin.this, "Unchecked", Toast.LENGTH_SHORT).show();

                }
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectionStart = password.getSelectionStart();
                int selectionEnd = password.getSelectionEnd();

                if (password.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }

                // Preserve cursor position
                password.setSelection(selectionStart, selectionEnd);
            }
        });



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String adminEmail = email.getText().toString().trim();
                String adminPassword = password.getText().toString().trim();

                if (adminEmail.isEmpty() || adminPassword.isEmpty()) {
                    Toast.makeText(AdminLogin.this, "Email and password are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!(fire.isChecked() || police.isChecked() || ambulance.isChecked() || coast.isChecked())){
                    Toast.makeText(AdminLogin.this, "Select the Account Type", Toast.LENGTH_SHORT).show();
                    return;
                }

                fAuth.signInWithEmailAndPassword(adminEmail, adminPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AdminLogin.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                    checkUserAccessLevel(task.getResult().getUser().getUid());
                                } else {
                                    Toast.makeText(AdminLogin.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AdminRegister.class));
            }
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordResetDialog();
            }
        });
    }

    private void checkUserAccessLevel(String uid) {
        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        DocumentReference df = firestore.collection("admins").document(uid);

        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d("TAG", "onSuccess " + documentSnapshot.getData());

                    String department = documentSnapshot.getString("Department");
                    if (department != null && !department.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleDepartmentAccess(department);
                            }
                        });
                    } else {
                        showToast("Department information not found");
                    }
                } else {
                    showToast("User data not found");
                }
            }
        });
    }

    private void handleDepartmentAccess(String department) {
        if (fire.isChecked() && "Fire".equals(department)) {
            startActivity(new Intent(getApplicationContext(), DashboardFireDepartment.class));
        } else if (ambulance.isChecked() && "Ambulance".equals(department)) {
            startActivity(new Intent(getApplicationContext(), DashboardAmbulanceDepartment.class));
        } else if (police.isChecked() && "Police".equals(department)) {
            startActivity(new Intent(getApplicationContext(), DashboardPoliceDepartment.class));
        } else if (coast.isChecked() && "Coast Guard".equals(department)) {
            startActivity(new Intent(getApplicationContext(), DashboardCoastGuardDepartment.class));
        } else {
            showToast("No matching department found");
        }
    }

    private void showToast(String message) {
        Toast.makeText(AdminLogin.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showPasswordResetDialog() {
        EditText resetMail = new EditText(this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter Your Email to Receive Reset Link");
        passwordResetDialog.setView(resetMail);

        passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mail = resetMail.getText().toString();
                sendPasswordResetEmail(mail);
            }
        });

        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User canceled the password reset.
            }
        });

        passwordResetDialog.create().show();
    }

    private void sendPasswordResetEmail(String email) {
        fAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminLogin.this, "The Link Sent To Your Email.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminLogin.this, "Error: Reset Link is Not Sent " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }
}