package com.example.resqapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.resqapp.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminLogin extends AppCompatActivity {

    EditText email, password;
    Button login;
    CheckBox rememberme;
    TextView createText, signup, forgotpass;
    FirebaseAuth fAuth;
    ImageView imageView;
    FirebaseFirestore firestore;
    private long TimeBack;
    public static final String SHARED_PREFS = "sharedPrefs";

    private static final String PREFS_NAME = "MyPrefs";
    private static final String LOGIN_EXPIRY = "loginExpiry";

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int CALL_PERMISSION_REQUEST_CODE = 1002;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1003;

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

        SharedPreferences preferences = getSharedPreferences("checkboxadmin", MODE_PRIVATE);
        String checkboxadmin = preferences.getString("remember", "");
        if (checkboxadmin.equals("true")) {
            long lastLoginTime = preferences.getLong("lastLoginTime", 0);
            long currentTime = System.currentTimeMillis();
            long thirtyDaysInMillis = 30 * 24 * 60 * 60 * 1000L;
            if (currentTime - lastLoginTime > thirtyDaysInMillis) {
                Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(AdminLogin.this, UserOrAdminLogin.class); // Redirect to login activity
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference docRef = db.collection("admins").document(userId);
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String department = documentSnapshot.getString("Department");
                            if (department != null) {
                                if (department.equals("Fire")) {
                                    Intent intent = new Intent(AdminLogin.this, DashboardFireDepartment.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else if (department.equals("Coast Guard")) {
                                    Intent intent = new Intent(AdminLogin.this, DashboardCoastGuardDepartment.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                                else if (department.equals("Ambulance")) {
                                    Intent intent = new Intent(AdminLogin.this, DashboardAmbulanceDepartment.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                                else if (department.equals("Police")) {
                                    Intent intent = new Intent(AdminLogin.this, DashboardPoliceDepartment.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    // Handle other departments or invalid department values
                                    Toast.makeText(AdminLogin.this, "Invalid department", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Handle null department field
                                Toast.makeText(AdminLogin.this, "Department field is null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle missing document
                            Toast.makeText(AdminLogin.this, "Admin document doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(AdminLogin.this, "Failed to retrieve admin document", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } else if (checkboxadmin.equals("false")) {
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
                    editor.putLong("lastLoginTime", System.currentTimeMillis()); // Store current time
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
                startActivity(new Intent(AdminLogin.this, AdminRegister.class));
            }
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordResetDialog();
            }
        });
        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CALL_PHONE, Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        } else {
            checkLocationEnabled();
        }
    }

    private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            // Prompt the user to enable location services
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE || requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) { // Update condition to include notification permission code
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationEnabled();
            }
        }
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            if (userEmail != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference adminsCollection = db.collection("admins");

                adminsCollection.whereEqualTo("Email", userEmail)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    // Assuming only one document is returned
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                    String department = documentSnapshot.getString("Department");
                                    if (department != null) {
                                        if ("Fire".equals(department)) {
                                            startActivity(new Intent(AdminLogin.this, DashboardFireDepartment.class));
                                        } else if ("Ambulance".equals(department)) {
                                            startActivity(new Intent(AdminLogin.this, DashboardAmbulanceDepartment.class));
                                        } else if ("Police".equals(department)) {
                                            startActivity(new Intent(AdminLogin.this, DashboardPoliceDepartment.class));
                                        } else if ("Coast Guard".equals(department)) {
                                            startActivity(new Intent(AdminLogin.this, DashboardCoastGuardDepartment.class));
                                        } else {
                                            showToast("Invalid department found for the user");
                                        }
                                    } else {
                                        showToast("Department field is null");
                                    }
                                } else {
                                    showToast("No matching document found for the user");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showToast("Error retrieving department information: " + e.getMessage());
                            }
                        });
            } else {
                showToast("User email is null");
            }
        } else {
            showToast("User is not authenticated");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - TimeBack > 1000){
            TimeBack = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "Press Again to Exit", Toast.LENGTH_SHORT).show();
        }
        super.onBackPressed();
    }
}