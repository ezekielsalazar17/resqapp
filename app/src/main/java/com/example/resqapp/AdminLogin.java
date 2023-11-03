package com.example.resqapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.resqapp.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class AdminLogin extends AppCompatActivity {

    EditText Number, Password, Otp;
    Button Login, GenerateOtp;
    TextView createText, signup, forgotpass;
    FirebaseAuth fAuth;
    CheckBox rememberme;
    public static final String SHARED_PREFS = "sharedPrefs";
    String verificationID;
    CheckBox fire, ambulance, police, coastguard;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.admin_login);

        Number = findViewById(R.id.number1);
        Password = findViewById(R.id.pw2);
        Login = findViewById(R.id.login_button1);
        GenerateOtp = findViewById(R.id.generateotp);
        Otp = findViewById(R.id.otp);
        createText = findViewById(R.id.donthaveaccount);
        signup = findViewById(R.id.signup);
        fAuth = FirebaseAuth.getInstance();
        forgotpass = findViewById(R.id.forgotpass);
        rememberme = findViewById(R.id.remember_me_checkbox);
        fire = findViewById(R.id.fire_checkbox);
        ambulance = findViewById(R.id.ambulance_checkbox);
        police = findViewById(R.id.police_checkbox);
        coastguard = findViewById(R.id.coastguard_checkbox);

        GenerateOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(Number.getText().toString())){
                    Toast.makeText(AdminLogin.this, "Enter a valid Phone Number ", Toast.LENGTH_SHORT).show();
                }else{
                    String number = Number.getText().toString();
                    sendVerificationcode(number);
                }

            }
        });

        Login.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(Otp.getText().toString())){
                    Toast.makeText(AdminLogin.this, "Wrong OTP Entered", Toast.LENGTH_SHORT).show();
                }else {
                    verifycode(Otp.getText().toString());
                }
            }
        }));

        signup.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), UserOrAdminRegister.class));
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter Your Email to Received Reset Link");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(AdminLogin.this, "Reset Link Sent To Your Mail.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AdminLogin.this, "Error ! Reset Link is Not Sent " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                passwordResetDialog.create().show();
            }
        });

    }

    private void verifycode(String Code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, Code);
        signinbyCredential(credential);
    }

    private void signinbyCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(AdminLogin.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void sendVerificationcode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(fAuth)
                        .setPhoneNumber("+63" + phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
    mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
           final String code = credential.getSmsCode();
           if(code!=null){
               verifycode(code);
           }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(AdminLogin.this, "Verification Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String s,
                @NonNull PhoneAuthProvider.ForceResendingToken token) {
           super.onCodeSent(s, token);
           verificationID = s;

        }
    };

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