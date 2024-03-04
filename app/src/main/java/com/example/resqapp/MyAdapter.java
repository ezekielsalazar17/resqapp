package com.example.resqapp;

import static com.example.resqapp.AdminRegister.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private Context context;
    private List<Item> items;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    private static final int LOCATION_PERMISSION_CODE = 101;

    public MyAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new MyViewHolder(itemView);
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void getCurrentLocation(MyViewHolder holder) {
        if (isLocationPermissionGranted()) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                holder.latitudeadminView.setText("Latitude: " + latitude);
                                holder.longitudeadminView.setText("Longitude: " + longitude);
                                getAddressFromLocation(holder, latitude, longitude);
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String userID = currentUser.getUid();
                                    updateUserData(userID, latitude, longitude, holder);
                                } else {
                                    Log.e("Firebase", "User not authenticated");
                                }
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private void getAddressFromLocation(MyViewHolder holder, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                holder.addressadminView.setText("Admin Address: " + addressLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUserData(String userID, double latitude, double longitude, MyViewHolder holder) {
        fStore.collection("admins").document(userID)
                .update("Latitude", latitude, "Longitude", longitude)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("Firebase", "Latitude and longitude stored successfully");
                        holder.latitudeadminView.setText("Latitude: " + latitude);
                        holder.longitudeadminView.setText("Longitude: " + longitude);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Error storing latitude and longitude", e);
                    }
                });
    }

    private void fetchUserData(String userID, MyViewHolder holder) {
        DocumentReference documentReference = fStore.collection("admins").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Firestore Error: " + error.getMessage());
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String address = documentSnapshot.getString("Admin Address");
                    double latitude = documentSnapshot.getDouble("Latitude");
                    double longitude = documentSnapshot.getDouble("Longitude");

                    holder.addressadminView.setText(address);
                    holder.latitudeadminView.setText(String.valueOf(latitude));
                    holder.longitudeadminView.setText(String.valueOf(longitude));
                } else {
                    Log.d(TAG, "No such document");
                }
            }
        });
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item currentItem = items.get(position);
        holder.nameView.setText("Name: " + currentItem.getFirstName() + " " + currentItem.getLastName());
        holder.addressView.setText("Address: " + currentItem.getAddress());
        holder.latitudeView.setText("Latitude: " + currentItem.getLatitude());
        holder.longitudeView.setText("Longitude: " + currentItem.getLongitude());
        holder.contactnumView.setText("Contact Number: " + currentItem.getContactNum());
        holder.latitudeadminView.setText("Latitude: " + currentItem.getLatitudeadmin());
        holder.longitudeadminView.setText("Longitude: " + currentItem.getLongitudeadmin());
        holder.addressadminView.setText("Admin Address: " + currentItem.getAddressadmin());


        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation(holder);
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Item clickedItem = items.get(adapterPosition);
                    Intent intent = new Intent(context, LocationSharingAdmin.class);
                    intent.putExtra("Address", clickedItem.getAddress());
                    intent.putExtra("Admin Address", clickedItem.getAddressadmin());
                    context.startActivity(intent);
                }
            }
        });

        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            String userID = user.getUid();
            fetchUserData(userID, holder);
        } else {
            Toast.makeText(context, "User not authenticated. Redirecting to login screen...", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Item> getItems() {
        return items;
    }

}