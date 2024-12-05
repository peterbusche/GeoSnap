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
import android.os.Handler;
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
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener{
    private LocationManager locationManager;
    //private TextView textview;
    private MapView mapView;
    private GeoPoint currentLocation;
    private Marker currentMarker;

    private TextView latLongTextView;
    private List<GeoPoint> trackedLocations; //store locations
    private Polyline polyline;  //connect markers
    private Handler handler; //handler for periodic tracking

    //FOR SIMULATION
    private List<GeoPoint> simulatedLocations; // Predefined list of locations
    private int simulatedIndex = 0; // Index to track current simulated location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set OsmDroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_main);

        // Initialize the MapView
        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        latLongTextView=(TextView)findViewById(R.id.latLongTextView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        trackedLocations = new ArrayList<>();
        polyline = new Polyline();
        polyline.setWidth(5f);
        polyline.setColor(0xFFFF0000);
        mapView.getOverlayManager().add(polyline);


        initializeSimulatedLocations();
        startLocationTracking();


    }


    // Predefined list of simulated locations (example coordinates)
    private void initializeSimulatedLocations() {
        // Predefined list of simulated locations (example coordinates)
        simulatedLocations = new ArrayList<>();
        simulatedLocations.add(new GeoPoint(43.6150, -116.2023)); // Boise
        simulatedLocations.add(new GeoPoint(43.6165, -116.2038)); // Nearby location 1
        simulatedLocations.add(new GeoPoint(43.6180, -116.2053)); // Nearby location 2
        simulatedLocations.add(new GeoPoint(43.6200, -116.2065)); // Nearby location 3
        simulatedLocations.add(new GeoPoint(43.6220, -116.2078)); // Nearby location 4
    }


    //check permissions
    private void startLocationTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //triggers inherited methods from LocationListener (onLocationChanged())
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            startPeriodicTracking();
        }
    }

//    private void startPeriodicTracking() {
//        handler = new Handler();
//        Runnable trackingRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if(trackedLocations.size() > 0) {
//                    GeoPoint lastLocation = trackedLocations.get(trackedLocations.size() - 1);
//                    addMarkerAndPolyline(lastLocation);
//                }
//                handler.postDelayed(this,5000); //repeat every 5 seconds
//            }
//        };
//        handler.post(trackingRunnable);
//    }

    private void startPeriodicTracking() {
        handler = new Handler();
        Runnable trackingRunnable = new Runnable() {
            @Override
            public void run() {
                if (simulatedIndex < simulatedLocations.size()) {
                    // Get the next simulated location
                    GeoPoint simulatedLocation = simulatedLocations.get(simulatedIndex);
                    simulatedIndex++;

                    // Use the simulated location to update the map
                    addMarkerAndPolyline(simulatedLocation);
//                    adjustZoomToFitAllMarkers();

                    // Update the TextView with simulated location
                    updateLatLongTextView(simulatedLocation.getLatitude(), simulatedLocation.getLongitude());
                } else {
                    // Stop the simulation if all locations are used
                    handler.removeCallbacks(this);
                }

                // Repeat every 5 seconds
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(trackingRunnable);
    }

    private void addMarkerAndPolyline(GeoPoint location) {
        //add new marker
        Marker marker = new Marker(mapView);
        marker.setPosition(location);
        marker.setTitle("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
        mapView.getOverlays().add(marker);

        //add location to polyline to update
        trackedLocations.add(location);
        polyline.setPoints(trackedLocations);
        mapView.invalidate(); //redraw the map
    }



    private void updateLatLongTextView(double latitude, double longitude) {
        String latLongText = String.format("Lat: %.5f, Long: %.5f", latitude, longitude);
        latLongTextView.setText(latLongText);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }














    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            GeoPoint newLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

            // Update the current location text
            latLongTextView.setText(String.format("Lat: %.5f, Long: %.5f", newLocation.getLatitude(), newLocation.getLongitude()));

            // Display current location and dynamically adjust zoom
            displayCurrentLocation(newLocation);
//            adjustZoomToFitAllMarkers();
        }
    }

    private void displayCurrentLocation(GeoPoint location) {
        // Zoom to current location only when the app starts
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(location);

        // Add a marker for the current location
        Marker marker = new Marker(mapView);
        marker.setPosition(location);
        marker.setTitle("Current Location");
        mapView.getOverlays().add(marker);

        // Update the TextView with current location
        updateLatLongTextView(location.getLatitude(), location.getLongitude());
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


}

