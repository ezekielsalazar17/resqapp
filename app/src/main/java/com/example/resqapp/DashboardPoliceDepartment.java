package com.example.resqapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardPoliceDepartment extends AppCompatActivity {

    ImageButton profile;
    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboardpolicedepartment);

        profile = findViewById(R.id.adminrofilepolice);

        profile.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), Policeprofile.class));
        });


    }
}