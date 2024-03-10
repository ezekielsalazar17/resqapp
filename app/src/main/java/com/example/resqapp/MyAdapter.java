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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
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

    private final Context context;
    private final List<Item> items;
    private GoogleMap gMap;

    private String adminAddress;
    private String adminLati;
    private String adminLongi;

    private String latitudeadminView;
    private String longitudeadminView;

    Geocoder geocoder;
    List<Address> listGeocoder;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;

    private static final int LOCATION_PERMISSION_CODE = 101;

    public MyAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


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

    private void getCurrentLocation() {

        if (isLocationPermissionGranted()) {
            // Use FusedLocationProviderClient to get the user's current location
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
                                // Get the latitude and longitude of the user's current location
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                // Update the TextViews with the new location
                                adminLati = String.valueOf(latitude);
                                adminLongi = String.valueOf(longitude);


                                // Get the address from latitude and longitude
                                getAddressFromLocation(latitude, longitude);

                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();

                                    firestore = FirebaseFirestore.getInstance(); // Get Firestore instance

                                    firestore.collection("admins").document(userId)
                                            .update("Latitude", latitude, "Longitude", longitude, "Address", getAddressFromLocation(latitude, longitude))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("Firebase", "Latitude and longitude stored successfully");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("Firebase", "Error storing latitude and longitude", e);
                                                }
                                            });
                                } else {
                                    // Handle case where user is not authenticated
                                    Log.e("Firebase", "User not authenticated");
                                }
                            } else {
                                // Request location permission if not granted
                                // ...
                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }

    }

    private Object getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                adminAddress = addressLine;
                return addressLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item currentItem = items.get(position);
        holder.nameView.setText("Name: " + currentItem.getFirstName() + " " + currentItem.getLastName());
        holder.addressView.setText("Address: " + currentItem.getAddress());
        holder.latitudeView.setText("Latitude: " + (currentItem.getLatitude()));
        holder.longitudeView.setText("Longitude: " + (currentItem.getLongitude()));
        holder.contactnumView.setText("Contact Number: " + (currentItem.getContactNum()));


        // Set OnClickListener for the ImageButton
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fetchLocationadmin();
                getCurrentLocation();
                // Handle item click
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Item clickedItem = items.get(adapterPosition);
                    Intent intent = new Intent(context, LocationSharingAdmin.class);

                    intent.putExtra("Address", clickedItem.getAddress());
                    intent.putExtra("Latitude", clickedItem.getLatitude());
                    intent.putExtra("Longitude", clickedItem.getLongitude());

                    intent.putExtra("Address Admin", adminAddress);
                    intent.putExtra("Latitude Admin", adminLati);
                    intent.putExtra("Longitude Admin",  adminLongi);
                    context.startActivity(intent); // Start activity using context
                }
            }
        });
    }

    private void fetchLocationadmin() {
        if (fAuth == null) {
            Log.e(TAG, "FirebaseAuth instance is null");
            return;
        }

        String userID = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firestore.collection("admins").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Firestore Error: " + error.getMessage());
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Data retrieved, update UI
                    String addressAdmin = documentSnapshot.getString("Address");
                    double latAdmin = documentSnapshot.getDouble("Latitude");
                    double longAdmin = documentSnapshot.getDouble("Longitude");

                    if (addressAdmin != null && !Double.isNaN(latAdmin) && !Double.isNaN(longAdmin)) {
                        // Set the fetched address to adminAddress field
                        adminLati = String.valueOf(latAdmin);
                        adminLongi = String.valueOf(longAdmin);
                        adminAddress = addressAdmin;

                        notifyDataSetChanged(); // Notify adapter that data has changed
                    }  else {
                        Log.d(TAG, "No such document");
                    }
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
}