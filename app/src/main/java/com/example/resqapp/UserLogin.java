package com.example.resqapp;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.resqapp.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;

public class UserLogin extends AppCompatActivity {

    private static final int REQUEST_CODE = 101010;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    EditText email, password;
    Button login;
    CheckBox rememberme;
    TextView createText, signup, forgotpass;
    FirebaseAuth fAuth;
    ConstraintLayout constraintLayout;
    ImageView fingerprint;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.user_login);

        email = findViewById(R.id.emailuser);
        password = findViewById(R.id.pw1);
        login = findViewById(R.id.login_button);
        createText = findViewById(R.id.donthaveaccount);
        signup = findViewById(R.id.signup);
        fAuth = FirebaseAuth.getInstance();
        constraintLayout = findViewById(R.id.eula_popup);
        forgotpass = findViewById(R.id.forgotpass);
        rememberme = findViewById(R.id.remember_me_checkbox);
        ImageView imageView = findViewById(R.id.imageView4);
        EditText password = findViewById(R.id.pw1);
        fingerprint = findViewById(R.id.fingerprint);




        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Fingerprint sensor not available", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Sensor is busy", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                startActivityForResult(enrollIntent, REQUEST_CODE);
                break;
        }

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(UserLogin.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);


                SharedPreferences sharedPreferences = getSharedPreferences("LoginDetails", MODE_PRIVATE);
                String userEmail = sharedPreferences.getString("email", "");
                String userPassword = sharedPreferences.getString("password", "");

                // Set email and password in corresponding text fields
                email.setText(userEmail);
                password.setText(userPassword);

                login.performClick();

                fAuth.signInWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    checkTypeofAccount(task.getResult().getUser().getUid());

                                } else {
                                    Toast.makeText(UserLogin.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        fingerprint.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
        });


        SharedPreferences preferences = getSharedPreferences("checkboxuser", MODE_PRIVATE);
        String checkboxuser = preferences.getString("remember", "");
        if (checkboxuser.equals("true")) {
            long lastLoginTime = preferences.getLong("lastLoginTime", 0);
            long currentTime = System.currentTimeMillis();
            long thirtyDaysInMillis = 30 * 24 * 60 * 60 * 1000L;
            if (currentTime - lastLoginTime > thirtyDaysInMillis) {
                Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserLogin.this, UserOrAdminLogin.class); // Redirect to login activity
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(UserLogin.this, DashboardUser.class);
                startActivity(intent);
                finish();
            }

        } else if (checkboxuser.equals("false")) {
            Toast.makeText(this, "Please Sign in", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
        }

        rememberme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences preferences = getSharedPreferences("checkboxuser", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if (isChecked) {
                    editor.putString("remember", "true");
                    editor.putLong("lastLoginTime", System.currentTimeMillis()); // Store current time
                    Toast.makeText(UserLogin.this, "Remember Me is Checked", Toast.LENGTH_SHORT).show();
                } else {
                    editor.putString("remember", "false");
                    Toast.makeText(UserLogin.this, "Unchecked", Toast.LENGTH_SHORT).show();
                }
                editor.apply();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userEmail = email.getText().toString().trim();
                final String userPassword = password.getText().toString().trim();

                if (userEmail.isEmpty() || userPassword.isEmpty()) {
                    Toast.makeText(UserLogin.this, "Email and password are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                fAuth.signInWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = fAuth.getCurrentUser();
                                    if (user.isEmailVerified()) {
                                        // Save email and password in SharedPreferences after successful login
                                        SharedPreferences sharedPreferences = getSharedPreferences("LoginDetails", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("email", userEmail);
                                        editor.putString("password", userPassword);
                                        editor.apply();

                                        // Proceed with your existing logic
                                        checkTypeofAccount(user.getUid());
                                    } else {
                                        Toast.makeText(UserLogin.this, "Please verify your email address first", Toast.LENGTH_SHORT).show();
                                        // You can also provide an option for the user to resend verification email here

                                        // Create and display a popup window
                                        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                        View popupView = inflater.inflate(R.layout.popupwindow_verification, null);

                                        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                                        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                        boolean focusable = true; // lets taps outside the popup also dismiss it

                                        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                                        // Set content for popup window
                                        TextView textView = popupView.findViewById(R.id.textView);
                                        textView.setText("Send a verification again");

                                        Button sendButton = popupView.findViewById(R.id.sendButton);
                                        sendButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // Resend verification email
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                if (user != null) {
                                                    user.sendEmailVerification()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(UserLogin.this, "Verification email sent successfully", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        Toast.makeText(UserLogin.this, "Failed to send verification email. Please try again later.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                                popupWindow.dismiss(); // Dismiss the popup window after sending email
                                            }
                                        });

                                        // Show the popup window
                                        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                                    }
                                } else {
                                    Toast.makeText(UserLogin.this, "Please Register Account First", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreatepopUpwindow();
            }
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordResetDialog();
            }
        });
    }
    public void checkTypeofAccount(String uid) {
        fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference df = firestore.collection("users").document(uid);

        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d("TAG", "onSuccess " + documentSnapshot.getData());

                    String typeofAccount = documentSnapshot.getString("Type of Account");
                    if (typeofAccount != null && !typeofAccount.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handletypeofAccount(typeofAccount);
                            }
                        });
                    } else {
                        showToast("Type of account is not found");
                    }
                } else {
                    showToast("User data not found");
                }
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(UserLogin.this, message, Toast.LENGTH_SHORT).show();
    }

    private void handletypeofAccount(String typeofAccount) {
        if("User".equals(typeofAccount)){
            startActivity(new Intent(UserLogin.this, DashboardUser.class));
        }else{
            showToast("No matching department found");
        }
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
                            Toast.makeText(UserLogin.this, "The Link Sent To Your Email.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserLogin.this, "Error: Reset Link is Not Sent " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private PopupWindow popupWindow;


    private void CreatepopUpwindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.eula, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        popupWindow = new PopupWindow(popUpView, width, height, focusable);
        constraintLayout.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(constraintLayout, Gravity.CENTER, 0, 0);
                Button decline, accept;
                decline = popUpView.findViewById(R.id.decline_button_eula);
                accept = popUpView.findViewById(R.id.accept_button_eula);

                decline.setOnClickListener((v) -> {
                    popupWindow.dismiss();

                });

                accept.setOnClickListener((v) -> {
                    startActivity(new Intent(UserLogin.this, UserRegister.class));
                });

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
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}