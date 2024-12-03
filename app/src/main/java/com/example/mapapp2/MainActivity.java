/*
Role of this file:
    Activity Lifecycle: Manage the lifecycle methods such as onCreate(), onStart(), and onResume().
    Logic: Respond to user interactions, such as button clicks.
    UI Control: Link XML layout files to Java code using setContentView() and manipulate
                UI elements via IDs.


Other Files in This Folder:
    You may have additional Java files for:
        Other Activities: For other screens in your app.
        Custom Classes: For specific logic, such as a helper class for calculations
                        or handling APIs
 */



package com.example.mapapp2;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.widget.Button;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;



public class MainActivity extends AppCompatActivity implements LocationListener{
    private LocationManager locationManager;
    //private TextView textview;
    private MapView mapView;
    private GeoPoint currentLocation;
    private Marker currentMarker;

    private TextView latLongTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set OsmDroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_main);

        // Initialize the MapView
        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);



        //initialize textview
        latLongTextView=(TextView)findViewById(R.id.latLongTextView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Buttons for zooming
//        Button zoomInButton = findViewById(R.id.zoomInButton);
//        Button zoomOutButton = findViewById(R.id.zoomOutButton);
//
//        // Set button click listeners
//        zoomInButton.setOnClickListener(v -> mapView.getController().zoomIn());
//        zoomOutButton.setOnClickListener(v -> mapView.getController().zoomOut());


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        //get permissions to access device
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        } else {
            requestLocationUpdates();
        }

    }
    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        }
    }
    private void displayCurrentLocation(Location location) {
        if (location == null) {
            Toast.makeText(this, "Unable to get location. Try again later.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Simulated current location (Boise, Idaho)
        double longitude=location.getLongitude();
        double latitude=location.getLatitude();

        // Set current location
        currentLocation = new GeoPoint(latitude, longitude);

        // Set map center and zoom level
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(currentLocation);

        // Update the marker
        if (currentMarker != null) {
            mapView.getOverlays().remove(currentMarker);
        }

        // Add a marker for the current location
        Marker marker = new Marker(mapView);
        marker.setPosition(currentLocation);
        marker.setTitle("You are here!");
        mapView.getOverlays().add(marker);

        // Update the TextView with latitude and longitude
        updateLatLongTextView(latitude, longitude);

        Toast.makeText(this, "Location: " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();
    }


    private void updateLatLongTextView(double latitude, double longitude) {
        String latLongText = String.format("Lat: %.5f, Long: %.5f", latitude, longitude);
        latLongTextView.setText(latLongText);
    }



// Registers the LocationListener to receive updates every second (1000ms)
//      or when the device moves 1 meter.
    @Override
    protected void onResume() {
        super.onResume();
        requestLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        displayCurrentLocation(location);
    }

//Called when a location provider (e.g., GPS) is enabled. Displays a toast message indicating that the provider is available.
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Provider enabled: " + provider, Toast.LENGTH_SHORT).show();
    }

//Called when a location provider is disabled.Displays a toast message indicating that the provider is unavailable.
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Provider disabled: " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
    }

}

