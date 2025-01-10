/*
app needs to prompt/force/make users enable "save location" in Camera settings on their phone
    -otherwise GPS location data wont be there


Followed this to get past external storage issues:
    https://stackoverflow.com/questions/62782648/android-11-scoped-storage-permissions
    -i dont think i can put this on google play store now because of this. Below is a suggestion
        for getting around this, but it needs much more code.
    -"Android 11 doesn't allow to access directly files from storage you must have to select file
        from storage and copy that file into your app package chache com.android.myapp. Below is the
        method to copy file from storage to app package cache"
 */



package com.example.mapapp2.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mapapp2.R;
import com.example.mapapp2.auth.AuthManager;
import com.example.mapapp2.models.PhotoMetadata;
import com.example.mapapp2.repository.MetadataCacheRepository;
import com.example.mapapp2.utils.FileUtils;
import com.example.mapapp2.utils.EXIFExtractor;
import com.example.mapapp2.utils.MetadataExample;
import com.example.mapapp2.repository.PhotoRepository;
import com.example.mapapp2.models.PhotoMetadata;

import com.google.android.gms.maps.OnMapReadyCallback;


import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private Button btnLogout, zoomInButton, zoomOutButton;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    private GoogleMap mMap;
    private List<PhotoMetadata> photoMetadataList;
    private Map<LatLng, List<PhotoMetadata>> locationMap = new HashMap<>(); //temporary - bad design (used to fix duplicates issue)


    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final int REQUEST_MANAGE_STORAGE = 2296;
    private static final String TAG = "MainActivityTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (!hasStorageAccess()) {
            requestStorageAccess();
        } else {
            Log.d(TAG, "Storage permissions already granted");
            //MetadataExample.extractMetadataFromMediaStore(this);
            fetchAndLogPhotoMetadata();
        }


        btnLogout = findViewById(R.id.btn_logout);
        zoomInButton = findViewById(R.id.zoom_in_button);
        zoomOutButton = findViewById(R.id.zoom_out_button);



        // Map initialization
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error initializing map", Toast.LENGTH_SHORT).show();
        }


        // Logout button logic
        btnLogout.setOnClickListener(v -> {
            AuthManager.clearAuthData(this);
            Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });

        // Set up zoom in/out button listeners
        zoomInButton.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        zoomOutButton.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        // Set up the bottom sheet
        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);


        // Set initial state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        bottomSheetBehavior.setPeekHeight(230); // Adjust as needed


        if (bottomSheetBehavior == null) {
            Log.e(TAG, "BottomSheetBehavior is null. Check your layout and initialization.");
        }

        // Optional: Add listeners for bottom sheet state changes
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d(TAG, "Bottom sheet expanded");
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d(TAG, "Bottom sheet collapsed");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.d(TAG, "Bottom sheet dragging");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d(TAG, "Bottom sheet hidden");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d(TAG, "Bottom sheet slide offset: " + slideOffset);
            }
        });

        // Example action inside the bottom sheet
        Button bottomSheetAction = findViewById(R.id.bottom_sheet_action);
        bottomSheetAction.setOnClickListener(v -> {
            Toast.makeText(this, "Bottom sheet action clicked!", Toast.LENGTH_SHORT).show();
        });


    }

    private boolean hasStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {   //for android 11 (API 30+)
            Log.i(TAG, "Route for Android 11 (hasStorageAccess())");
            return Environment.isExternalStorageManager();  //checks AndroidManifest for MANAGE_EXTERNAL_STORAGE permission
        } else {    //for android 10 and below
            Log.i(TAG, "Route for Android 10 (hasStorageAccess())");
            int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return readPermission == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { //for android 11 (API 30+)
            try {
                Log.i(TAG, "Route for Android 11 (requestStorageAccess())");
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
            } catch (Exception e) {
                Log.d(TAG, "requestStorageAccess has been denied");
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
            }
        } else {    //for android 10 and below
            Log.i(TAG, "Route for Android 10 (requestStorageAccess())");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_STORAGE_PERMISSION);
        }
    }

    private void fetchAndLogPhotoMetadata() {
        PhotoRepository photoRepository = new PhotoRepository(this);
        photoMetadataList = photoRepository.fetchPhotoMetadata();

        if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
            for (PhotoMetadata metadata : photoMetadataList) {
                Log.d(TAG, "Photo Metadata: " +
                        "File Path: " + metadata.getFilePath() +
                        ", Latitude: " + metadata.getLatitude() +
                        ", Longitude: " + metadata.getLongitude() +
                        ", Timestamp: " + metadata.getTimestamp());
            }
        } else {
            Log.d(TAG, "No photo metadata available.");
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                Log.d(TAG, "Manage storage permission granted");
                //MetadataExample.extractMetadataFromMediaStore(this);
                fetchAndLogPhotoMetadata();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Improper Request Code: onActivityResult()");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permissions granted");
                //MetadataExample.extractMetadataFromMediaStore(this);
                fetchAndLogPhotoMetadata();
            } else {
                Log.i(TAG, "Permission denied");
                Toast.makeText(this, "Storage permissions denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Improper Request Code: onRequestPermissionsResult()");
        }
    }

    private void setTestCameraPosition() {
        LatLng testLocation = new LatLng(43.599222222222224, -116.24865); // Your test coordinates
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(testLocation, 15)); // Adjust zoom level as needed
    }


    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initialCameraPosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Get the Fused Location Provider Client
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            // Get the last known location
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                } else {
                    Toast.makeText(this, "Unable to fetch current location.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }





    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set initial camera position
        //initialCameraPosition();
        setTestCameraPosition();

        // Add markers to the map
        addPhotoMarkers();

        // Set up marker click listener
        setupMarkerClickListener();

    }




    private void addPhotoMarkers() {
        Log.i(TAG, "addPhotoMarkers(): ");

        if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
            // Clear the map and locationMap to avoid duplicates on reload
            mMap.clear();
            locationMap.clear();

            // Group images by GPS coordinates
            for (PhotoMetadata metadata : photoMetadataList) {
                if (metadata.getLatitude() != 0 && metadata.getLongitude() != 0) {
                    LatLng location = new LatLng(metadata.getLatitude(), metadata.getLongitude());
                    locationMap.computeIfAbsent(location, k -> new ArrayList<>()).add(metadata);
                } else {
                    Log.i(TAG, "No GPS metadata for file: " + metadata.getFilePath());
                }
            }

            // Add a single marker for each unique location
            for (Map.Entry<LatLng, List<PhotoMetadata>> entry : locationMap.entrySet()) {
                LatLng location = entry.getKey();
                List<PhotoMetadata> metadataList = entry.getValue();

                // Use the title to indicate the number of images at this location
                String markerTitle = "Photos: " + metadataList.size();
                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(markerTitle));
            }

            Log.i(TAG, "Markers added for " + locationMap.size() + " unique locations.");
        } else {
            Log.d(TAG, "No photo metadata available for map markers.");
        }
    }

    private void setupMarkerClickListener() {
        mMap.setOnMarkerClickListener(marker -> {
            LatLng markerPosition = marker.getPosition();

            // Retrieve the list of photos for this marker's position
            List<PhotoMetadata> metadataList = locationMap.get(markerPosition);

            if (metadataList != null && !metadataList.isEmpty()) {
                StringBuilder info = new StringBuilder("Photos at this location:\n");
                for (PhotoMetadata metadata : metadataList) {
                    info.append(metadata.getFilePath()).append("\n");
                }
                // Display the information in a Toast or a dialog
                //Toast.makeText(this, info.toString(), Toast.LENGTH_LONG).show();

                ImageDialogFragment dialog = ImageDialogFragment.newInstance(metadataList);
                dialog.show(getSupportFragmentManager(), "ImageDialog");

            } else {
                Log.d(TAG, "No photos found for this marker.");
            }

            return false; // Returning false allows the default behavior (e.g., camera movement) to occur
        });
    }



}

