package com.example.mapapp2;

import com.example.mapapp2.BuildConfig;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;
import android.content.Context;
import android.widget.TextView;


import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;


import org.maplibre.android.snapshotter.MapSnapshot;
import org.maplibre.android.snapshotter.MapSnapshotter;
import org.maplibre.android.geometry.LatLngBounds;
import android.graphics.Bitmap;
import android.os.Environment;

import java.util.ArrayList;
import java.util.List;




public class MainActivity extends AppCompatActivity implements LocationListener{

    private MapView mapView;
    private TextView latLongTextView;
    private MapLibreMap mapLibreMap;
    private LocationManager locationManager;
    private Handler handler;
    private List<LatLng> trackedLocations;
    private boolean useSimulatedData = false;
    private int simulatedIndex=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the API Key from BuildConfig
        String apiKey = BuildConfig.MAPTILER_API_KEY;

        //get style from website
        String mapId = "topo-v2";
        String styleUrl = "https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + apiKey;

        // Initialize MapLibre
        MapLibre.getInstance(this);

        // Inflate the layout
        setContentView(R.layout.activity_main);

        // Initialize UI
        mapView = findViewById(R.id.mapView);
        latLongTextView = findViewById(R.id.latLongTextView);
        mapView.onCreate(savedInstanceState);

        // Initialize tracking structures
        trackedLocations = new ArrayList<>();

        // Set up the map with the desired style
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapLibreMap map) {
                mapLibreMap = map;
                mapLibreMap.setStyle(new Style.Builder().fromUri(styleUrl), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        if (useSimulatedData) {
                            initializeSimulatedLocations();
                            startSimulatedTracking();
                        } else {
                            checkAndRequestPermissions();
                        }
                    }
                });
            }
        });
    }

    private void initializeSimulatedLocations() {
        trackedLocations.add(new LatLng(43.6150, -116.2023)); // Boise
        trackedLocations.add(new LatLng(43.6165, -116.2038)); // Nearby location 1
        trackedLocations.add(new LatLng(43.6180, -116.2053)); // Nearby location 2
        trackedLocations.add(new LatLng(43.6200, -116.2065)); // Nearby location 3
        trackedLocations.add(new LatLng(43.6220, -116.2078)); // Nearby location 4
    }

    private void startSimulatedTracking() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (simulatedIndex < trackedLocations.size()) {
                    LatLng location = trackedLocations.get(simulatedIndex);
                    simulatedIndex++;
                    updateLocation(location);
                    handler.postDelayed(this, 5000); // Update every 5 seconds
                }
            }
        }, 5000);
    }

    private void checkAndRequestPermissions() {
        Log.d("Permissions", "Permission granted: " + (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startRealTimeTracking();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    //It is triggered after calling ActivityCompat.requestPermissions(...).
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Permission granted: true");
                startRealTimeTracking();
            } else {
                Log.d("Permissions", "Permission granted: false");
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRealTimeTracking() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        }
    }

    private void updateLocation(LatLng location) {
        if (mapLibreMap != null) {
            // Add a marker
            mapLibreMap.getStyle(style -> {
                mapLibreMap.addMarker(new org.maplibre.android.annotations.MarkerOptions().position(location));
            });

            // Update UI
            latLongTextView.setText(String.format("Lat: %.5f, Lon: %.5f", location.getLatitude(), location.getLongitude()));

            // Move the camera
            CameraPosition position = new CameraPosition.Builder()
                    .target(location)
                    .zoom(15.0) // Zoom level
                    .build();
            mapLibreMap.setCameraPosition(position);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!useSimulatedData && location != null) {
            updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }








    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
        if (locationManager != null) locationManager.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}

