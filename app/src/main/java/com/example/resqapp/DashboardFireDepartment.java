package com.example.resqapp;

import static com.example.resqapp.AdminRegister.TAG;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFireDepartment extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private ImageButton profileButton;
    private ImageView picture_user;
    private ImageView imageButton;
    private MyAdapter adapter;
    private static final String PREF_NAME = "user_image_pref";
    private static final String KEY_IMAGE_URI = "image_uri";
    Switch account;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String SWITCH_STATE_KEY = "switch_state";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboardfiredepartment);
        getSupportActionBar().hide();

        // Initialize Firebase instances
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Initialize views
        profileButton = findViewById(R.id.adminprofilefire);
        imageButton = findViewById(R.id.accept_button); // Initialize ImageButton
        picture_user = findViewById(R.id.picture_user);
        account = findViewById(R.id.accountonoff);

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Initialize empty list of items
        List<Item> items = new ArrayList<>();

        profileButton.setOnClickListener((v) -> {
            startActivity(new Intent(getApplicationContext(), Fireprofile.class));
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        boolean previousSwitchState = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);
        account.setChecked(previousSwitchState);

        account.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Switch is turned on, set background color to green
                    Toast.makeText(DashboardFireDepartment.this, "Turn on Notification", Toast.LENGTH_SHORT).show();
                    account.getThumbDrawable().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.MULTIPLY);
                    account.getTrackDrawable().setColorFilter(getResources().getColor(R.color.light_green), PorterDuff.Mode.MULTIPLY);
                    checkForNewEmergency();
                } else{
                    // Switch is turned off, set background color to red
                    Toast.makeText(DashboardFireDepartment.this, "Turn off Notification", Toast.LENGTH_SHORT).show();
                    account.getThumbDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.MULTIPLY);
                    account.getTrackDrawable().setColorFilter(getResources().getColor(R.color.light_red), PorterDuff.Mode.MULTIPLY);
                }
                sharedPreferences.edit().putBoolean(SWITCH_STATE_KEY, isChecked).apply();
            }
        });

        // Initialize Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String historyCollection = "pendingfiredept";

        // Check if user is signed in
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUid();
            // Fetch data from Firestore
            db.collection(historyCollection)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            List<Item> userHistoryList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : snapshots) {
                                // Retrieve data from Firestore document
                                String firstName = document.getString("firstName");
                                String lastName = document.getString("lastName");
                                firstName = capitalizeEveryWord(firstName);
                                lastName = capitalizeEveryWord(lastName);

                                String userEmail = document.getString("useremail");

                                String address = document.getString("address");
                                address = capitalizeEveryWord(address); // Capitalize the address

                                Double latitudeObj = document.getDouble("latitude");
                                Double longitudeObj = document.getDouble("longitude");
                                String contactNumObj = document.getString("contactNum");

                                String timestamp = document.getString("timestamp");

                                String contactNum = contactNumObj != null ? String.valueOf(contactNumObj) : "0";
                                double latitude = latitudeObj != null ? latitudeObj.doubleValue() : 0.0;
                                double longitude = longitudeObj != null ? longitudeObj.doubleValue() : 0.0;

                                Item item = new Item(userEmail, firstName, lastName, address, latitude, longitude, contactNum, timestamp);
                                userHistoryList.add(item);
                            }

                            // Sort the list based on timestamp
                            Collections.sort(userHistoryList, new Comparator<Item>() {
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); // Adjust format according to your timestamp format

                                @Override
                                public int compare(Item item1, Item item2) {
                                    try {
                                        Date date1 = dateFormat.parse(item1.getTimestamp());
                                        Date date2 = dateFormat.parse(item2.getTimestamp());
                                        return date1.compareTo(date2);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        return 0;
                                    }
                                }
                            });

                            // Update RecyclerView adapter with the new data
                            MyAdapter adapter = new MyAdapter(DashboardFireDepartment.this, userHistoryList);
                            recyclerView.setAdapter(adapter);
                        }
                    });
        } else {
            // Handle the case when the user is not signed in
            // You might want to redirect the user to the login screen or take appropriate action
            // For now, let's just log the error
            Log.e(TAG, "User not signed in.");
        }

    }

    private void checkForNewEmergency() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Check for new documents added to the "pendingfiredept" collection
        db.collection("pendingfiredept")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // If there are new documents, call the showNotification function
                        showNotification();
                    }
                });
    }

    private void showNotification() {
        final String CHANNEL_ID = "channel1";
        final int NOTIFICATION_ID = 1;

        Intent activityIntent = new Intent(this, DashboardFireDepartment.class);
        PendingIntent playContentIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

        // Create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification";
            String description = "FIRE DEPARTMENT";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("New Emergency")
                .setContentText("EMERGENCY")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("We need you to respond now!"))
                .addAction(R.mipmap.ic_launcher, "Go to Fire Dashboard", playContentIntent)
                .setColor(Color.RED)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }


    // Method to capitalize every word in a string
    private String capitalizeEveryWord(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Split the text by spaces
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();

        // Capitalize the first letter of each word
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(" ");
            }
        }
        // Remove trailing space
        return result.toString().trim();
    }
    ////////// CHANGE FIRE BUTTON IMAGE ///////////
    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            Uri photoUrl = currentUser.getPhotoUrl();
            // Save the image URI in SharedPreferences
            saveImageUri(photoUrl.toString());
        }

        profileButton();
    }
    private Bitmap getCircleBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int diameter = Math.min(width, height);

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, (width - diameter) / 2, (height - diameter) / 2, diameter, diameter);

        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, diameter, diameter);
        final RectF rectF = new RectF(rect);
        final float roundPx = diameter / 2;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(croppedBitmap, rect, rect, paint);

        return output;
    }

    private void saveImageUri(String imageUri) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_IMAGE_URI, imageUri);
        editor.apply();
    }
    private void profileButton() {
        // Fetch the saved image URI from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("image_pref_fire", MODE_PRIVATE);
        String savedImageUriString = preferences.getString("image_uri_fire", null);
        if (savedImageUriString != null) {
            Uri savedImageUri = Uri.parse(savedImageUriString);
            // Load the saved image into ImageView
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), savedImageUri);
                // Apply circular mask to the bitmap
                Bitmap circularBitmap = getCircleBitmap(bitmap);
                profileButton.setImageBitmap(circularBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private String getCurrentUserID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is signed in
        if (user != null) {
            // Get the user's ID
            return user.getUid();
        } else {
            // User is not signed in, handle this case according to your application logic
            return null;
        }
    }

    public void onSwitchClick(View view) {
        if (account.isChecked()) {
            Toast.makeText(this, "Notification On", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Notification Off", Toast.LENGTH_SHORT).show();
        }
    }

}