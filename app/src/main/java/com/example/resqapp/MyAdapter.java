package com.example.resqapp;

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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private Context context;
    private List<Item> items;
    private GoogleMap gMap;

    Geocoder geocoder;
    List<Address> listGeocoder;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;

    private static final int LOCATION_PERMISSION_CODE = 101;

    public MyAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;

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
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Get the latitude and longitude of the user's current location
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                // Update the TextViews with the new location
                                latitudeadminView.setText("Latitude: " + latitude);
                                longi.setText("Longitude: " + longitude);


                                // Get the address from latitude and longitude
                                getAddressFromLocation(latitude, longitude);

                                // Add marker to the map at the user's current location
                                LatLng userLocation = new LatLng(latitude, longitude);
                                gMap.clear(); // Clear existing markers
                                gMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

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
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }

    }

    private Object getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                addressTextView.setText("Address: " + addressLine);
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
        holder.addressadminView.setText("Address: " + currentItem.getAddressAdmin());
        holder.latitudeadminView.setText("Latitude: " + (currentItem.getLatitudeAdmin()));
        holder.longitudeadminView.setText("Longitude: " + (currentItem.getLongitudeAdmin()));
        holder.contactnumView.setText("Contact Number: " + (currentItem.getContactNum()));


        // Set OnClickListener for the ImageButton
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getCurrentLocation();
                // Handle item click
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Item clickedItem = items.get(adapterPosition);
                    Intent intent = new Intent(context, LocationSharingAdmin.class);
                    intent.putExtra("Address", clickedItem.getAddress());
                    intent.putExtra("AddressAdmin", clickedItem.getAddressAdmin());
                    context.startActivity(intent); // Start activity using context
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
}
