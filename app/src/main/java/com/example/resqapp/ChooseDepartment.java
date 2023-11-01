package com.example.resqapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class ChooseDepartment extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    Button fire, police, ambulance, coast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_department);

        fire = findViewById(R.id.fire_button_admin);
        police = findViewById(R.id.police_button_admin);
        ambulance = findViewById(R.id.ambulance_button_admin);
        coast = findViewById(R.id.coast_guard_button_admin);

    }
}