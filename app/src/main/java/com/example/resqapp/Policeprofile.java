package com.example.resqapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class Policeprofile extends AppCompatActivity {
    private static final String TAG = "Policeprofile";
    private TextView contactNum, department, email;
    private FirebaseAuth fAuth;
    private Button policeconedit;
    private ImageView policepic;
    private FirebaseFirestore fStore;
    private Uri imageUri;
    private Uri savedImageUri;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String userID;
    private View Logout;
    FloatingActionButton policepicedit;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_police_admin);

        getSupportActionBar().hide();

        contactNum = findViewById(R.id.police_contact_number);
        department = findViewById(R.id.police_department);
        email = findViewById(R.id.police_email);
        policeconedit = findViewById(R.id.edit_contact2);
        policepic = findViewById(R.id.policepicture);
        policepicedit = findViewById(R.id.police_profile_change);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        savedImageUri = null;

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
        DocumentReference documentReference = fStore.collection("admins").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Firestore Error: " + error.getMessage());
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Data retrieved, update UI
                    String contactNumber = documentSnapshot.getString("Contact Number");
                    String dept = documentSnapshot.getString("Department");
                    String userEmail = documentSnapshot.getString("Email");

                    // Update UI elements
                    contactNum.setText(contactNumber);
                    department.setText("Department: " + dept);
                    email.setText("Email: " + userEmail);
                } else {
                    Log.d(TAG, "No such document");
                }
            }
        });


        ////////////////////////////// POLICE CHANGE PICTURE ///////////////////////////////

        policepicedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Policeprofile.this)
                        .setTitle("Confirm Selection")
                        .setMessage("Are you sure you want to select an image?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ImagePicker.with(Policeprofile.this)
                                        .crop()
                                        .compress(1024)
                                        .maxResultSize(1080, 1080)
                                        .start();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        // Retrieve saved image URI from SharedPreferences and load into ImageView
        SharedPreferences preferences = getSharedPreferences("image_pref_police", MODE_PRIVATE);
        String savedImageUriString = preferences.getString("image_uri_police", null);
        if (savedImageUriString != null) {
            Uri savedImageUri = Uri.parse(savedImageUriString);
            // Load the saved image into ImageView
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), savedImageUri);
                // Apply circular mask to the bitmap
                Bitmap circularBitmap = getCircleBitmap(bitmap);
                policepic.setImageBitmap(circularBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //////////// LOG OUT /////////////
        Logout = findViewById(R.id.sign_out);

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the sign_out button is clicked
                if (v.getId() == R.id.sign_out) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.putLong("lastLoginTime", System.currentTimeMillis());
                    editor.apply();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), UserOrAdminLogin.class));
                    finish();
                }
            }
        });

        policeconedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a dialog or start a new activity to edit the contact number
                // For example, you can use an AlertDialog to get user input
                AlertDialog.Builder builder = new AlertDialog.Builder(Policeprofile.this);
                builder.setTitle("Edit Contact Number");

                final EditText input = new EditText(Policeprofile.this);
                input.setInputType(InputType.TYPE_CLASS_PHONE);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newNumber = input.getText().toString().trim();
                        contactNum.setText(newNumber);

                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            String userID = currentUser.getUid();
                            DocumentReference userRef = FirebaseFirestore.getInstance().collection("admins").document(userID);
                            userRef.update("Contact Number", newNumber)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Contact number updated successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Error updating contact number", e);
                                        }
                                    });
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImagePicker.REQUEST_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();

            saveImageUriToPreferences(imageUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                Bitmap circularBitmap = getCircleBitmap(bitmap);
                policepic.setImageBitmap(circularBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            uploadImageToStorage(imageUri);
        }
    }

    private void saveImageUriToPreferences(Uri uri) {
        SharedPreferences preferences = getSharedPreferences("image_pref_police", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (uri != null) {
            editor.putString("image_uri_police", uri.toString());
        } else {
            editor.remove("image_uri_police");
        }
        editor.apply();
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

    private void uploadImageToStorage(Uri imageUri) {
        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("profileImages_police").child(userID + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(Policeprofile.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Policeprofile.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
