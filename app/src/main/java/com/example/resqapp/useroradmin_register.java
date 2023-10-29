package com.example.resqapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class useroradmin_register extends AppCompatActivity {

    Button user, admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.useroradmin);

        user = findViewById(R.id.user_button);
        admin = findViewById(R.id.admin_button);

        user.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), user_register.class));
        });

        admin.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), admin_register.class));
        });
    }
}