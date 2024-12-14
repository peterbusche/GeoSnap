package com.example.mapapp2;

import com.example.mapapp2.BuildConfig;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
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
import org.maplibre.android.camera.CameraUpdateFactory;

import org.maplibre.android.snapshotter.MapSnapshot;
import org.maplibre.android.snapshotter.MapSnapshotter;
import org.maplibre.android.geometry.LatLngBounds;
import android.graphics.Bitmap;
import android.os.Environment;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;





public class MainActivity extends AppCompatActivity implements LocationListener{

    private static final String TAG = "MyAppLogs";
    private MapView mapView;
    private TextView latLongTextView;
    private Button zoomInButton, zoomOutButton, snapshotButton;
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
        zoomInButton = findViewById(R.id.zoom_in_button);
        zoomOutButton = findViewById(R.id.zoom_out_button);
        snapshotButton = findViewById(R.id.snapshot_button);
        mapView.onCreate(savedInstanceState);

        // Initialize tracking structures
        trackedLocations = new ArrayList<>();


        //LAMBDA
        mapView.getMapAsync(map -> {
            mapLibreMap = map;
            mapLibreMap.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
                mapLibreMap.getUiSettings().setZoomGesturesEnabled(true); // Enable zoom gestures
                mapLibreMap.getUiSettings().setScrollGesturesEnabled(true); // Enable scrolling
                mapLibreMap.getUiSettings().setDoubleTapGesturesEnabled(true); // Enable double-tap zoom



                if (useSimulatedData) {
                    initializeSimulatedLocations();
                    startSimulatedTracking();
                } else {
                    checkAndRequestPermissions();
                }
            });
        });


        // Set up snapshot button click listener
        snapshotButton.setOnClickListener(v -> {
            if (mapLibreMap != null) {
                takeSnapshot();
            }
        });

        // Set up zoom button click listeners
        zoomInButton.setOnClickListener(v -> {
            if (mapLibreMap != null) {
                CameraPosition currentPosition = mapLibreMap.getCameraPosition();
                CameraPosition newPosition = new CameraPosition.Builder(currentPosition)
                        .zoom(currentPosition.zoom + 1) // Increase zoom level
                        .build();
                mapLibreMap.setCameraPosition(newPosition);
            }
        });

        zoomOutButton.setOnClickListener(v -> {
            if (mapLibreMap != null) {
                CameraPosition currentPosition = mapLibreMap.getCameraPosition();
                CameraPosition newPosition = new CameraPosition.Builder(currentPosition)
                        .zoom(currentPosition.zoom - 1) // Decrease zoom level
                        .build();
                mapLibreMap.setCameraPosition(newPosition);
            }
        });

    }
            /*
        ANONYMOUS CLASSES:
            1)Functional Interface
                ex1) mapView.getMapAsync(new OnMapReadyCallback() {
                    -If working with a FUNCTIONAL INTERFACE:
                        -will only have single abstract method to worry about
                        -for example, Runnable() interface

            2)Not Functional Interface
                -Must implement all abstract methods of the interface or abstract class


        LAMBDA EXPRESSIONS:
            1)CAN ONLY BE USED WITH FUNCTIONAL INTERFACES
        */







//    // Method to take a snapshot
//    private void takeSnapshot() {
//        Toast.makeText(this, "Snap Taken!", Toast.LENGTH_SHORT).show();
//        if (mapLibreMap == null) {
//            Log.e("Snapshot", "MapLibreMap is not initialized.");
//            return;
//        }
//
//        Log.e(TAG, "Here");
//        // Define the region for the snapshot
//        LatLngBounds bounds = new LatLngBounds.Builder()
//                .include(new LatLng(43.6150, -116.2023)) // Example region
//                .include(new LatLng(43.6220, -116.2078)) // Example region
//                .build();
//
//        Log.e(TAG, "Here1");
//        // Create a camera position for the snapshot
//        CameraPosition position = new CameraPosition.Builder()
//                .target(new LatLng(43.6150, -116.2023)) // Center point
//                .zoom(15.0) // Zoom level
//                .build();
//
//        Log.e(TAG, "Here2");
//        // Configure snapshot options
//        MapSnapshotter.Options options = new MapSnapshotter.Options(800, 800)
//                .withStyle(mapLibreMap.getStyle().getUri()) // Use the current style
//                .withRegion(bounds) // Specify the region
//                .withCameraPosition(position) // Specify the camera position
//                .withLogo(false); // Disable logo if preferred
//
//        Log.e(TAG, "Here3");
//        // Create the snapshotter
//        MapSnapshotter snapshotter = new MapSnapshotter(this, options);
//
//        Log.e(TAG, "Here4");
//        // Start the snapshot
//        snapshotter.start(new MapSnapshotter.SnapshotReadyCallback() {
//            @Override
//            public void onSnapshotReady(MapSnapshot snapshot) {
//                Log.e(TAG, "Here4.1");
//                Bitmap bitmap = snapshot.getBitmap();
//                Log.e(TAG, "Here4.2");
//                // Save the bitmap to a file
//                saveSnapshotToFile(bitmap);
//                Log.e(TAG, "Here4.3");
//            }
//        });
//        Log.e(TAG, "Here5");
//    }



    // Method to save the bitmap to a file
    private void saveSnapshotToFile(Bitmap snapshot) {
        Log.e(TAG, "Here6");
        // Directory to save the snapshot
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MapSnapshots");
        if (!directory.exists()) {
            Log.e(TAG, "Here7");
            directory.mkdirs(); // Create the directory if it doesn't exist
        }

        // File name for the snapshot
        String fileName = "snapshot_" + System.currentTimeMillis() + ".png";
        File file = new File(directory, fileName);

        FileOutputStream fileOutputStream = null;
        try {
            Log.e(TAG, "Here8");
            fileOutputStream = new FileOutputStream(file);
            // Compress the bitmap and save it as a PNG
            snapshot.compress(CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            Toast.makeText(this, "Snapshot saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Here9");
            e.printStackTrace();
            Toast.makeText(this, "Failed to save snapshot", Toast.LENGTH_SHORT).show();
        } finally {
            Log.e(TAG, "Here10");
            if (fileOutputStream != null) {
                try {
                    Log.e(TAG, "Here11");
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Here12");
                    e.printStackTrace();
                }
            }
        }
        Log.e(TAG, "Here13");
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

