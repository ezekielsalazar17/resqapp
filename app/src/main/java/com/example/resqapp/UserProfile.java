package com.example.resqapp;

import static com.example.resqapp.UserRegister.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class UserProfile extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 100;
    private static final int CROP_IMAGE_REQUEST_CODE = 200;
    private Uri imageUri;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private TextView name, idnum, unumber, bday, email;
    Button Logout;
    ImageView preview;
    FloatingActionButton select;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_user);

        name = findViewById(R.id.user_name_profile);
        unumber = findViewById(R.id.user_contact_number);
        bday =findViewById(R.id.user_birthday1);
        email = findViewById(R.id.user_email1);

        Logout = findViewById(R.id.sign_out);
        preview = findViewById(R.id.previewimage);
        select = findViewById(R.id.selectimg);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(UserProfile.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            fetchUserData(userID);
        } else {
            // Redirect user to login screen or handle as per your app's logic
            Toast.makeText(this, "User not authenticated. Redirecting to login screen...", Toast.LENGTH_SHORT).show();
            // Example: startActivity(new Intent(this, LoginActivity.class));
        }

    }
    private void fetchUserData(String userID) {
        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Firestore Error: " + error.getMessage());
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String fullname = documentSnapshot.getString("First Name "+ "Last Name");
                    String unum = documentSnapshot.getString("Contact Number");
                    String bday1 = documentSnapshot.getString("Birthday");
                    String email1 = documentSnapshot.getString("Email");

                    // Update UI elements
                    name.setText(fullname);
                    unumber.setText(unum);
                    bday.setText(bday1);
                    email.setText(email1);
                } else {
                    Log.d(TAG, "No such document");
                }
            }
        });


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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        preview.setImageURI(uri);
    }
}