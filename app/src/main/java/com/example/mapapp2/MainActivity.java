package com.example.mapapp2;
import com.example.mapapp2.SnapshotHandler;

import com.example.mapapp2.BuildConfig;

import android.Manifest;
import android.content.Intent;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.view.View;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import android.view.View;
import android.view.ViewTreeObserver;




public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "MyAppLogs";
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private LocationManager locationManager;
    private Handler handler;


    //.xml button variables
    private TextView latLongTextView;
    private Button zoomInButton, zoomOutButton, snapshotButton, closeSnapshotButton, imageButton, closeImageButton;
    FrameLayout snapshotContainer, imageContainer;
    ImageView snapshotImageView, imageImageView;
    private MapSnapshotter mapSnapshotter;
    private ImageHandler imageHandler;


    //tracking data variables
    private List<LatLng> trackedLocations;
    private boolean useSimulatedData = false;
    private int simulatedIndex=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the API Key from BuildConfig
        String apiKey = BuildConfig.MAPTILER_API_KEY;

        // Get style from website
        String mapId = "topo-v2";
        String styleUrl = "https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + apiKey;

        // Initialize MapLibre
        MapLibre.getInstance(this);

        // Inflate the layout
        setContentView(R.layout.activity_main);

        // Initialize UI components
        mapView = findViewById(R.id.mapView);

        snapshotContainer = findViewById(R.id.snapshot_container);
        snapshotImageView = findViewById(R.id.snapshotImageView);
        imageContainer = findViewById(R.id.image_container);
        imageImageView = findViewById(R.id.imageImageView);

        latLongTextView = findViewById(R.id.latLongTextView);
        zoomInButton = findViewById(R.id.zoom_in_button);
        zoomOutButton = findViewById(R.id.zoom_out_button);
        snapshotButton = findViewById(R.id.snapshot_button);
        closeSnapshotButton = findViewById(R.id.close_snapshot_button);
        imageButton = findViewById(R.id.image_button);
        closeImageButton = findViewById(R.id.close_image_button);

        imageHandler = new ImageHandler(this);


        // Validate UI components
        if (snapshotContainer == null || snapshotImageView == null) {
            Log.e(TAG, "Snapshot components are not properly initialized.");
            return;
        }

        mapView.onCreate(savedInstanceState);

        // Initialize the map
        mapView.getMapAsync(map -> {
            mapLibreMap = map;
            mapLibreMap.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
                mapLibreMap.getUiSettings().setZoomGesturesEnabled(true);
                mapLibreMap.getUiSettings().setScrollGesturesEnabled(true);
                mapLibreMap.getUiSettings().setDoubleTapGesturesEnabled(true);

                if (useSimulatedData) {
                    initializeSimulatedLocations();
                    startSimulatedTracking();
                } else {
                    checkAndRequestPermissions();
                }
            });
        });




        // Set up the snapshot button functionality
        snapshotButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SnapshotHandler.class);
            startActivity(intent); // Start the SnapshotHandler activity
        });

        // Set up close button
        closeSnapshotButton.setOnClickListener(v -> {
            snapshotContainer.setVisibility(View.GONE);
            snapshotImageView.setImageBitmap(null); // Clear the bitmap
        });





        imageButton.setOnClickListener(v -> imageHandler.displayImage("nakedmolerat-001.jpg", imageContainer, imageImageView));
        closeImageButton.setOnClickListener(v -> imageHandler.closeImage(imageContainer, imageImageView));
        // Set up the IMAGE button functionality
//        imageButton.setOnClickListener(v -> {
//            try {
//                // Load the image from the assets folder
//                AssetManager assetManager = getAssets();
//                InputStream inputStream = assetManager.open("nakedmolerat-001.jpg"); // Replace with your image name
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//
//                // Set the image in the ImageView
//                imageImageView.setImageBitmap(bitmap);
//
//                // Show the snapshot container
//                imageContainer.setVisibility(View.VISIBLE);
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e(TAG, "Failed to load image from assets: ");
//            }
//        });
//
//        // Set up close button
//        closeImageButton.setOnClickListener(v -> {
//            imageContainer.setVisibility(View.GONE);
//            imageImageView.setImageBitmap(null); // Clear the bitmap
//        });



        // Set up zoom buttons
        zoomInButton.setOnClickListener(v -> {
            if (mapLibreMap != null) {
                CameraPosition currentPosition = mapLibreMap.getCameraPosition();
                mapLibreMap.setCameraPosition(new CameraPosition.Builder(currentPosition).zoom(currentPosition.zoom + 1).build());
            }
        });

        zoomOutButton.setOnClickListener(v -> {
            if (mapLibreMap != null) {
                CameraPosition currentPosition = mapLibreMap.getCameraPosition();
                mapLibreMap.setCameraPosition(new CameraPosition.Builder(currentPosition).zoom(currentPosition.zoom - 1).build());
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