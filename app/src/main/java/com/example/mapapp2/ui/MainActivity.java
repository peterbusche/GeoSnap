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
import android.widget.ImageView;
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

//for google photos api
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.Task;

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
    private LinearLayout bottomSheet;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    private GoogleMap mMap;
    private List<PhotoMetadata> photoMetadataList;
    private Map<LatLng, List<PhotoMetadata>> locationMap = new HashMap<>(); //temporary - bad design (used to fix duplicates issue)


    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final int REQUEST_MANAGE_STORAGE = 2296;

    private static final int REQUEST_GOOGLE_SIGN_IN = 2001;
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
            populateImageScrollView();
        }


        btnLogout = findViewById(R.id.btn_logout);
        zoomInButton = findViewById(R.id.zoom_in_button);
        zoomOutButton = findViewById(R.id.zoom_out_button);
        bottomSheet = findViewById(R.id.bottom_sheet);


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


        //Bottom Sheet Functionality
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Dynamically adjust peek height relative to navigation bar
        LinearLayout navigationBar = findViewById(R.id.bottom_navigation_parent);
        navigationBar.post(() -> { //use this to wait for navigation bar to load in UI thread before calculating bottom sheet height
            int navigationBarHeight = navigationBar.getHeight();
            bottomSheetBehavior.setPeekHeight(navigationBarHeight - 50); // 50dp for the drag handle
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


    private void populateImageScrollView() {
        LinearLayout imageScrollContainer = findViewById(R.id.image_scroll_container);

        if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
            for (PhotoMetadata metadata : photoMetadataList) {
                // Create an ImageView for each photo
                ImageView imageView = new ImageView(this); //this will persist in memory since it is attached to MainActivity.java?
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500, 500); // Adjust size as needed
                layoutParams.setMargins(16, 0, 16, 0); // Add spacing between images
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // Load the image using its URI
                imageView.setImageURI(Uri.parse(metadata.getFilePath()));

                // Add click listener for the image
                imageView.setOnClickListener(v -> {
                    if (metadata.getLatitude() != 0 && metadata.getLongitude() != 0) {
                        LatLng imageLocation = new LatLng(metadata.getLatitude(), metadata.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(imageLocation, 20)); // Adjust zoom level as needed
                        Toast.makeText(this, "Moved to image location: " + metadata.getFilePath(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No GPS data available for this image.", Toast.LENGTH_SHORT).show();
                    }
                });

                // Add the ImageView to the horizontal scroll container
                imageScrollContainer.addView(imageView);
            }
        } else {
            Log.d(TAG, "No photo metadata available to populate the horizontal scroll view.");
        }
    }

}

