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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.resqapp.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserLogin extends AppCompatActivity {

    EditText email, password;
    Button login;
    CheckBox rememberme;
    TextView createText, signup, forgotpass;
    FirebaseAuth fAuth;
    ConstraintLayout constraintLayout;
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
                String userEmail = email.getText().toString().trim();
                String userPassword = password.getText().toString().trim();

                if (userEmail.isEmpty() || userPassword.isEmpty()) {
                    Toast.makeText(UserLogin.this, "Email and password are required", Toast.LENGTH_SHORT).show();
                    return;
                }

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