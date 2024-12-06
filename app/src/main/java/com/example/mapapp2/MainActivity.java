package com.example.mapapp2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private MapView mapView;
    private TextView latLongTextView;
    private LocationManager locationManager;
    private Handler handler;

    private List<GeoPoint> trackedLocations;
    private Polyline polyline;

    // Simulated Data
    private List<GeoPoint> simulatedLocations;
    private int simulatedIndex = 0;

    private boolean useSimulatedData = true; // Toggle between simulated and real-time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set OsmDroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_main);

        // Initialize UI
        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        latLongTextView = findViewById(R.id.latLongTextView);

        // Initialize data structures
        trackedLocations = new ArrayList<>();
        polyline = new Polyline();
        polyline.setWidth(5f);
        polyline.setColor(0xFFFF0000); // Red line
        mapView.getOverlayManager().add(polyline);

        // Choose between simulated or real-time data
        if (useSimulatedData) {
            initializeSimulatedLocations();
            startSimulatedTracking();
        } else {
            startRealTimeTracking();
        }
    }

    private void initializeSimulatedLocations() {
        simulatedLocations = new ArrayList<>();
        simulatedLocations.add(new GeoPoint(43.6150, -116.2023)); // Boise
        simulatedLocations.add(new GeoPoint(43.6165, -116.2038)); // Nearby location 1
        simulatedLocations.add(new GeoPoint(43.6180, -116.2053)); // Nearby location 2
        simulatedLocations.add(new GeoPoint(43.6200, -116.2065)); // Nearby location 3
        simulatedLocations.add(new GeoPoint(43.6220, -116.2078)); // Nearby location 4
    }

    private void startSimulatedTracking() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (simulatedIndex < simulatedLocations.size()) {
                    GeoPoint location = simulatedLocations.get(simulatedIndex);
                    simulatedIndex++;
                    updateLocation(location);

                    mapView.getController().setCenter(location);
                    mapView.getController().setZoom(15.0);

                    handler.postDelayed(this, 5000);
                }
            }
        }, 5000);
    }

    private void startRealTimeTracking() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void updateLocation(GeoPoint location) {
        // Add marker
        Marker marker = new Marker(mapView);
        marker.setPosition(location);
        marker.setTitle("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
        mapView.getOverlays().add(marker);

        // Update polyline
        trackedLocations.add(location);
        polyline.setPoints(trackedLocations);

        // Center the map on the current location
        mapView.getController().setCenter(location);
        mapView.getController().setZoom(10.0);

        // Update UI
        latLongTextView.setText(String.format("Lat: %.5f, Lon: %.5f", location.getLatitude(), location.getLongitude()));

        mapView.invalidate(); // Redraw the map
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!useSimulatedData && location != null) {
            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            updateLocation(geoPoint);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (mapView != null) {
            mapView.onDetach();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Provider enabled: " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Provider disabled: " + provider, Toast.LENGTH_SHORT).show();
    }
}
