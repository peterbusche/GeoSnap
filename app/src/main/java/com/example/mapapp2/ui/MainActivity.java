/*
PROMPT/FORCE/MAKE users enable "save location" in Camera settings on their phone
    -otherwise GPS location data wont be there
 */

package com.example.mapapp2.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import com.example.mapapp2.R;
import com.example.mapapp2.auth.AuthManager;
import com.example.mapapp2.models.PhotoMetadata;
import com.example.mapapp2.repository.MetadataCacheRepository;
import com.example.mapapp2.utils.FileUtils;
import com.example.mapapp2.utils.EXIFExtractor;
import com.example.mapapp2.utils.MetadataExample;

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

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;




import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private Button btnLogout, zoomInButton, zoomOutButton;

    private GoogleMap mMap;

    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final String TAG = "MainActivityTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Show rationale if necessary
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Storage permission is needed to access photos.", Toast.LENGTH_SHORT).show();
            }

            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            // Permission already granted
            Log.i(TAG, "Permission already granted");
            //testEXIFExtractor();
            MetadataTest();
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

    }


    private void MetadataTest() {
        MetadataExample.extractMetadataFromMediaStore(this);
    }



    private void testEXIFExtractor(){
        String testPath = "/storage/emulated/0/Pictures/1.jpg"; // Replace with the actual file path
        double[] latLng = EXIFExtractor.extractLatLng(testPath);
        if (latLng != null) {
            Log.d(TAG, "Extracted Latitude: " + latLng[0] + ", Longitude: " + latLng[1]);
        } else {
            Log.d(TAG, "No GPS data found in: " + testPath);
        }
    }



    private void testEXIFExtractor2() {
        // Query MediaStore for all indexed images
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null, // No selection filter
                null, // No selection arguments
                MediaStore.Images.Media.DATE_TAKEN + " DESC" // Sort by most recent
        );

        if (cursor == null) {
            Log.d(TAG, "Cursor is null. No images found on the device.");
            Toast.makeText(this, "No images found on the device.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (cursor.moveToFirst()) {
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                do {
                    String filePath = cursor.getString(dataColumn);
                    Log.i(TAG, "Processing file: " + filePath);

                    // Use EXIFExtractor to process the file
                    File testFile = new File(filePath);
                    if (testFile.exists()) {
                        double[] latLng = EXIFExtractor.extractLatLng(filePath);
                        if (latLng != null) {
                            Log.d(TAG, "Extracted Latitude: " + latLng[0] + ", Longitude: " + latLng[1]);
                            Toast.makeText(this, "File: " + filePath + "\nLatitude: " + latLng[0] + ", Longitude: " + latLng[1], Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "No GPS data found in the image: " + filePath);
                        }
                    } else {
                        Log.e(TAG, "File does not exist or is inaccessible: " + filePath);
                    }
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No images found on the device.");
                Toast.makeText(this, "No images found on the device.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing images: ", e);
        } finally {
            cursor.close();
        }
    }






    private void testEXIFExtractor1() {

        Log.i(TAG, "EXIF EXTRACTOR: START");
        // Query the MediaStore for images
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_TAKEN + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(dataColumn);
            cursor.close();

            // Now use this file path with EXIFExtractor
            File testFile = new File(filePath);
            if (testFile.exists()) {
                double[] latLng = EXIFExtractor.extractLatLng(filePath);
                if (latLng != null) {
                    Log.d(TAG, "Extracted Latitude: " + latLng[0] + ", Longitude: " + latLng[1]);
                    Toast.makeText(this, "Latitude: " + latLng[0] + ", Longitude: " + latLng[1], Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "No GPS data found in the image.");
                    Toast.makeText(this, "No GPS data found in the image.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "Image file not found: " + filePath);
                Toast.makeText(this, "Image file not found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "No images found on the device.");
            Toast.makeText(this, "No images found on the device.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted");
                testEXIFExtractor();
            } else {
                Log.i(TAG, "Permission denied");
                Toast.makeText(this, "Permission is required to access photos.", Toast.LENGTH_SHORT).show();
            }
        }
    }










    private void testMetadataCacheAndFileUtils() {
        MetadataCacheRepository cacheRepository = new MetadataCacheRepository(this);

        // Test Metadata Cache
        List<PhotoMetadata> testMetadata = new ArrayList<>();
        testMetadata.add(new PhotoMetadata("/path/to/image.jpg", 40.7128, -74.0060, System.currentTimeMillis()));
        cacheRepository.saveMetadataToCache(testMetadata);

        List<PhotoMetadata> loadedMetadata = cacheRepository.loadMetadataFromCache();
        Log.d("MainActivity", "Loaded metadata size: " + loadedMetadata.size());

        cacheRepository.clearCache();
        Log.d("MainActivity", "Cleared metadata cache.");

        // Test FileUtils
        String testFilePath = "/path/to/image.jpg";
        boolean fileExists = FileUtils.doesFileExist(testFilePath);
        Log.d("MainActivity", "File exists: " + fileExists);

        long fileSize = FileUtils.getFileSize(testFilePath);
        Log.d("MainActivity", "File size: " + fileSize);

        boolean deleted = FileUtils.deleteFile(testFilePath);
        Log.d("MainActivity", "File deleted: " + deleted);
    }




    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set initial camera position
        initialCameraPosition();
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

}

