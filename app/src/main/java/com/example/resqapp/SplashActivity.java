package com.example.resqapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class

SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Timer timer;
    private int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar2);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (i<100){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    progressBar.setProgress(i);
                    i++;
                }else {
                    timer.cancel();
                    Intent intent = new Intent(SplashActivity.this, UserOrAdminLogin.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, 0, 20);



    }
}