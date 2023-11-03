package com.example.resqapp;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;

public class
DashboardUser extends AppCompatActivity{
    public static final String SHARED_PREFS = "sharedPrefs";
    Button locationSharing, logout;
    ImageButton firebutton,profilebutton;


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);


        locationSharing = findViewById(R.id.location_tracking);
        firebutton = findViewById(R.id.fire_button);
        logout = findViewById(R.id.logout);
        profilebutton = findViewById(R.id.profile_button);

        locationSharing.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), AccessLocUser.class));
        });

        firebutton.setOnClickListener((v) -> {
            ;
        });
        profilebutton.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), UserProfile.class));
        });

        // Remove the call to the finish() method
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember", "false");
                editor.apply();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), UserLogin.class));

            }
        });



    }

}
