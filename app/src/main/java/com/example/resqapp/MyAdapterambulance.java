package com.example.resqapp;

import static com.example.resqapp.AdminRegister.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyAdapterambulance extends RecyclerView.Adapter<MyViewHolderambulance> {

    private final Context context;
    private final List<Itemambulance> items;
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

    public MyAdapterambulance(Context context, List<Itemambulance> items) {
        this.context = context;
        this.items = items;
        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


    }

    @NonNull
    @Override
    public MyViewHolderambulance onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new MyViewHolderambulance(itemView);
    }


    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void getCurrentLocation(LocationListener listener) {
        if (isLocationPermissionGranted() && isLocationEnabled()) {
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

                                // Update Firestore with the new location
                                updateFirestoreWithLocation(latitude, longitude);
                                listener.onLocationFetched(latitude, longitude);
                            } else {
                                // Location is null, request location permission if not granted
                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                            }
                        }
                    });
        } else {
            // Location permission not granted or location services not enabled, request permission or prompt user to enable location
            if (!isLocationPermissionGranted()) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            } else {
                // Location permission granted, but location services not enabled
                showLocationSettingsDialog();
            }
        }
    }

    private void updateFirestoreWithLocation(double latitude, double longitude) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // Get Firestore instance

            firestore.collection("admins").document(userId)
                    .update("Latitude", latitude, "Longitude", longitude, "Admin Address", getAddressFromLocation(latitude, longitude))
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
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Location services are disabled. Do you want to enable them?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
    public void onBindViewHolder(@NonNull MyViewHolderambulance holder, int position) {
        Itemambulance currentItem = items.get(position);

        if (currentItem != null) {
            // Bind the item's data to the view
            holder.nameView.setText("Name: " + currentItem.getFirstName() + " " + currentItem.getLastName());
            holder.useremailView.setText("User Email: " + currentItem.getUserEmail());
            holder.addressView.setText("Address: " + currentItem.getAddress());
            holder.latitudeView.setText("Latitude: " + (currentItem.getLatitude()));
            holder.longitudeView.setText("Longitude: " + (currentItem.getLongitude()));
            holder.contactnumView.setText("Contact Number: " + (currentItem.getContactNum()));
            holder.timestampView.setText("Timestamp: " + currentItem.getTimestamp());

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performOperationsSynchronously(currentItem);
                }
            });
        }
    }

    private void performOperationsSynchronously(Itemambulance currentItem) {
        getCurrentLocation(new LocationListener() {
            @Override
            public void onLocationFetched(double latitude, double longitude) {
                fetchLocationadmin(new LocationFetchListener() {
                    @Override
                    public void onLocationFetch(String address, String latitude, String longitude) {
                        inProgressfetching(new OnCompleteListener1(){
                            @Override
                            public void onComplete() {
                                addToFireDeptUserCollection(new OnCompleteListener() {
                                    @Override
                                    public void onComplete() {
                                        startLocationSharingAdminActivity(currentItem, address, latitude, longitude);
                                    }
                                }, currentItem);
                            }
                        }, currentItem);
                    }
                });
            }
        });
    }

    private void inProgressfetching(OnCompleteListener1 listener, Itemambulance currentItem) {

        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // Fetch documents from "pendingfiredept" collection
        db.collection("pendingambulancedept")
                .whereEqualTo("useremail", currentItem.getUserEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String address = documentSnapshot.getString("address");
                            Double latitude = documentSnapshot.getDouble("latitude");
                            Double longitude = documentSnapshot.getDouble("longitude");
                            String contactNum = documentSnapshot.getString("contactNum");
                            String userEmail = currentItem.getUserEmail();

                            // Get document data
                            Map<String, Object> data = documentSnapshot.getData();
                            data.put("firstName", firstName);
                            data.put("lastName", lastName);
                            data.put("address", address);
                            data.put("latitude", latitude);
                            data.put("longitude", longitude);
                            data.put("contactNum", contactNum);
                            data.put("User Email", userEmail);

                            // Add document data to "firedeptHistory" collection
                            db.collection("inprogressAmbulance")
                                    .add(data)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "Document added to collection 'firedeptuser' with ID: " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle failure
                                        }
                                    });
                            db.collection("pendingambulancedept")
                                    .document(documentSnapshot.getId())
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Document deleted successfully
                                            listener.onComplete();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle failure
                                        }
                                    });
                            break;
                        }
                    }
                });
    }
    private void addToFireDeptUserCollection(OnCompleteListener listener, Itemambulance currentItem) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Define the collection name for user data
        String adminCollection = "admins";

        // Get the current user ID (assuming you have it)
        String userID = getCurrentUserID(); // Replace with your method to get the user ID

        db.collection(adminCollection)
                .document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            String Department = documentSnapshot.getString("Department");
                            String Address = documentSnapshot.getString("Admin Address");
                            Double latitude = documentSnapshot.getDouble("Latitude");
                            Double longitude = documentSnapshot.getDouble("Longitude");
                            String contactNum = documentSnapshot.getString("Contact Number");
                            String userEmail = currentItem.getUserEmail();

                            Map<String, Object> historyData = new HashMap<>();
                            historyData.put("Admin Address", Address);
                            historyData.put("Department", Department);
                            historyData.put("Latitude", latitude);
                            historyData.put("Longitude", longitude);
                            historyData.put("Contact Number", contactNum);
                            historyData.put("User Email", userEmail);

                            db.collection("ambulancedeptuser")
                                    .add(historyData)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "Document added to collection 'firedeptuser' with ID: " + documentReference.getId());
                                            listener.onComplete();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Error adding document to collection 'firedeptuser': " + e.getMessage());
                                        }
                                    });
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    }
                });
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

    private void startLocationSharingAdminActivity(Itemambulance currentItem, String address, String latitude, String longitude) {
        Intent intent = new Intent(context, LocationSharingAdminAmbulance.class);
        intent.putExtra("Address", currentItem.getAddress());
        intent.putExtra("Latitude", currentItem.getLatitude());
        intent.putExtra("Longitude", currentItem.getLongitude());
        intent.putExtra("Address Admin", address);
        intent.putExtra("Latitude Admin", latitude);
        intent.putExtra("Longitude Admin", longitude);
        intent.putExtra("UserEmail", currentItem.getUserEmail());
        context.startActivity(intent);
    }

    private void fetchLocationadmin(LocationFetchListener listener) {

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
                    String addressAdmin = documentSnapshot.getString("Admin Address");
                    double latAdmin = documentSnapshot.getDouble("Latitude");
                    double longAdmin = documentSnapshot.getDouble("Longitude");

                    if (addressAdmin != null && !Double.isNaN(latAdmin) && !Double.isNaN(longAdmin)) {
                        // Notify listener with updated location
                        listener.onLocationFetch(addressAdmin, String.valueOf(latAdmin), String.valueOf(longAdmin));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                }
            }
        });
    }

    interface LocationListener {
        void onLocationFetched(double latitude, double longitude);
    }

    interface LocationFetchListener {
        void onLocationFetch(String address, String latitude, String longitude);
    }

    interface OnCompleteListener {
        void onComplete();
    }
    interface OnCompleteListener1{
        void onComplete();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}