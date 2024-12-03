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



        //get permissions to access device
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        } else {
            startLocationTracking();
        }
    }

    private void startLocationTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            startPeriodicTracking();
        }
    }

    private void startPeriodicTracking() {
        handler = new Handler();
        Runnable trackingRunnable = new Runnable() {
            @Override
            public void run() {
                if(trackedLocations.size() > 0) {
                    GeoPoint lastLocation = trackedLocations.get(trackedLocations.size() - 1);
                    addMarkerAndPolyline(lastLocation);
                }
                handler.postDelayed(this,5000); //repeat every 5 seconds
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

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            GeoPoint newLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

            // Update the current location text
            latLongTextView.setText(String.format("Lat: %.5f, Long: %.5f", newLocation.getLatitude(), newLocation.getLongitude()));

            // Display current location and dynamically adjust zoom
            displayCurrentLocation(location);
            adjustZoomToFitAllMarkers();
        }
    }

//    @Override
//    public void onLocationChanged(Location location) {
//        if(location != null) {
//            GeoPoint newLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
//
//            // Update the current location text
//            latLongTextView.setText(String.format("Lat: %.5f, Long: %.5f", newLocation.getLatitude(), newLocation.getLongitude()));
//
//            // Add the new location to tracked locations
//            trackedLocations.add(newLocation);
//        }
//    }

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

        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

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

        // Add to tracked locations if tracking is active
        if (trackedLocations != null && !trackedLocations.contains(currentLocation)) {
            trackedLocations.add(currentLocation);
            updatePolyline();
        }

        // Update the TextView with latitude and longitude
        updateLatLongTextView(latitude, longitude);

        Toast.makeText(this, "Location: " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();
    }

    private void updatePolyline() {
        // Update the polyline with all tracked locations
        polyline.setPoints(trackedLocations);
        mapView.invalidate(); // Redraw the map
    }

    private void adjustZoomToFitAllMarkers() {
        if (trackedLocations.isEmpty()) {
            return;
        }

        // Calculate a bounding box to fit all tracked locations
        double minLat = Double.MAX_VALUE, maxLat = Double.MIN_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = Double.MIN_VALUE;

        for (GeoPoint point : trackedLocations) {
            minLat = Math.min(minLat, point.getLatitude());
            maxLat = Math.max(maxLat, point.getLatitude());
            minLon = Math.min(minLon, point.getLongitude());
            maxLon = Math.max(maxLon, point.getLongitude());
        }

        mapView.zoomToBoundingBox(
                new org.osmdroid.util.BoundingBox(maxLat, maxLon, minLat, minLon), true
        );
    }



//    private void displayCurrentLocation(Location location) {
//        if (location == null) {
//            Toast.makeText(this, "Unable to get location. Try again later.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        // Simulated current location (Boise, Idaho)
//        double longitude=location.getLongitude();
//        double latitude=location.getLatitude();
//
//        // Set current location
//        currentLocation = new GeoPoint(latitude, longitude);
//
//        // Set map center and zoom level
//        mapView.getController().setZoom(15.0);
//        mapView.getController().setCenter(currentLocation);
//
//        // Update the marker
//        if (currentMarker != null) {
//            mapView.getOverlays().remove(currentMarker);
//        }
//
//        // Add a marker for the current location
//        Marker marker = new Marker(mapView);
//        marker.setPosition(currentLocation);
//        marker.setTitle("You are here!");
//        mapView.getOverlays().add(marker);
//
//        // Update the TextView with latitude and longitude
//        updateLatLongTextView(latitude, longitude);
//
//        Toast.makeText(this, "Location: " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();
//    }


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

//    @Override
//    public void onLocationChanged(Location location) {
//        displayCurrentLocation(location);
//    }

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

