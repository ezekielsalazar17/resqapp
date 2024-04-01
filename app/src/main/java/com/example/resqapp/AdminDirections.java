package com.example.resqapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminDirections extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMapadmin;
    private String userLocation;
    private String adminLocation;

    TextView lat, longi, latadmin1, longiadmin1;

    Geocoder geocoder;
    List<Address> listGeocoder;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;

    private List<LatLng> routeCoordinates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directions_admin);

        getSupportActionBar().hide();

        lat = findViewById(R.id.userlati1);
        longi = findViewById(R.id.userlongi1);

        latadmin1 = findViewById(R.id.adminlat1);
        longiadmin1 = findViewById(R.id.adminlongi1);

        userLocation = getIntent().getStringExtra("user_location");

        double lat1 = getIntent().getDoubleExtra("user_latitude", 0.0);
        lat.setText(String.valueOf(lat1));
        double lat2 = getIntent().getDoubleExtra("user_longitude", 0.0);
        longi.setText(String.valueOf(lat2));

        adminLocation = getIntent().getStringExtra("admin_location");

        String latadmins = getIntent().getStringExtra("admin_latitude");
        latadmin1.setText(latadmins);
        String longiadmins = getIntent().getStringExtra("admin_longitude");
        longiadmin1.setText(String.valueOf(longiadmins));

        geocoder = new Geocoder(this, Locale.getDefault());
        if (isLocationPermissionGranted()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMapadmin = googleMap;

        double latitude = Double.parseDouble(lat.getText().toString());
        double longitude = Double.parseDouble(longi.getText().toString());

        double latitude1 = Double.parseDouble(latadmin1.getText().toString());
        double longitude1 = Double.parseDouble(longiadmin1.getText().toString());

        LatLng userLatlong = new LatLng(latitude, longitude);
        LatLng adminLatLong = new LatLng(latitude1, longitude1);

        gMapadmin.addMarker(new MarkerOptions().position(userLatlong).title("Emergency Location (User)"));
        gMapadmin.addMarker(new MarkerOptions().position(adminLatLong).title("Admin Location"));
        gMapadmin.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatlong, 10));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gMapadmin.setMyLocationEnabled(true);
        }

        gMapadmin.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        getRouteCoordinates(adminLatLong, userLatlong);
    }

    private void getRouteCoordinates(LatLng start, LatLng end) {
        String apiKey = "AIzaSyC7m6Ggei6Q9SysKXbmqEHPt-W_nyQ42Vg";

        String urlString = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + start.latitude + "," + start.longitude +
                "&destination=" + end.latitude + "," + end.longitude +
                "&key=" + apiKey;

        new DirectionsTask().execute(urlString);
    }

    private void drawRoute(List<LatLng> routeCoordinates) {
        if (routeCoordinates != null && !routeCoordinates.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(routeCoordinates);
            polylineOptions.width(10);
            polylineOptions.color(Color.BLUE);
            gMapadmin.addPolyline(polylineOptions);
        } else {
            Log.e("AdminDirections", "Route coordinates are empty");
        }
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private class DirectionsTask extends AsyncTask<String, Void, List<LatLng>> {

        @Override
        protected List<LatLng> doInBackground(String... strings) {
            List<LatLng> coordinates = new ArrayList<>();
            HttpURLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                    JsonArray routes = jsonResponse.getAsJsonArray("routes");
                    if (routes.size() > 0) {
                        for (JsonElement routeElement : routes) {
                            JsonObject route = routeElement.getAsJsonObject();
                            JsonObject overviewPolyline = route.getAsJsonObject("overview_polyline");
                            String encodedPolyline = overviewPolyline.get("points").getAsString();
                            List<LatLng> decodedPolyline = decodePoly(encodedPolyline);
                            coordinates.addAll(decodedPolyline);
                        }
                    } else {
                        Log.e("DirectionsTask", "No routes found in the response.");
                    }
                } else {
                    Log.e("DirectionsTask", "HTTP error: " + connection.getResponseCode() + " " + connection.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("DirectionsTask", "Error occurred while fetching directions: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return coordinates;
        }

        @Override
        protected void onPostExecute(List<LatLng> routeCoordinates) {
            if (!routeCoordinates.isEmpty()) {
                drawRoute(routeCoordinates);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng latLng : routeCoordinates) {
                    builder.include(latLng);
                }
                LatLngBounds bounds = builder.build();
                int padding = 100;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                gMapadmin.animateCamera(cu);
            } else {
                Log.e("DirectionsTask", "Route coordinates are empty.");
            }
        }
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }
        return poly;
    }
}
