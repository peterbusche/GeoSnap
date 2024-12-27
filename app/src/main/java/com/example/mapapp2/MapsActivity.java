package com.example.mapapp2;
import com.example.mapapp2.databinding.ActivityMapsBinding;

//anroid imports
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;
import android.graphics.Bitmap;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;


//java
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



//google imports
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

//API imports
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static String TAG = "MapAppLogs";
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FrameLayout snapshotContainer;
    private ImageView snapshotImage;
    private Button closeSnapshotButton;
    ApiService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //initialize map
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = ApiClient.getClient().create(ApiService.class);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        // Initialize components
        snapshotContainer = findViewById(R.id.snapshot_container);
        snapshotImage = findViewById(R.id.snapshot_image);
        closeSnapshotButton = findViewById(R.id.close_snapshot_button);


        // Find the buttons
        Button zoomInButton = findViewById(R.id.zoom_in_button);
        Button zoomOutButton = findViewById(R.id.zoom_out_button);
        Button snapButton = findViewById(R.id.snap_button);
        Button apiButton = findViewById(R.id.api_button);
        apiButton.setOnClickListener(v -> fetchDataFromApi());

        snapButton.setOnClickListener(v -> takeSnapshot());

        closeSnapshotButton.setOnClickListener(v -> {
            snapshotContainer.setVisibility(View.GONE);
            snapshotImage.setImageBitmap(null); // Clear the image
        });


        // Set click listeners
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

    private void fetchDataFromApi() {
        Call<PingResponse> call = apiService.pingServer();

        call.enqueue(new Callback<PingResponse>() {
            @Override
            public void onResponse(Call<PingResponse> call, Response<PingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PingResponse data = response.body();
                    Log.d(TAG, "API Response: " + data.getStatus() + ",  " + data.getMessage());
                } else {
                    Log.e(TAG, "API Error: Response Code = " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PingResponse> call, Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
            }
        });
    }








    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        initialCameraPosition();
    }

    private void takeSnapshot() {
        if (mMap != null) {
            // Center camera on current location with specific zoom
            LatLng currentLocation = new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    // Take a snapshot after the camera animation is complete
                    mMap.snapshot(bitmap -> {
                        if (bitmap != null) {
                            snapshotImage.setImageBitmap(bitmap);
                            snapshotContainer.setVisibility(View.VISIBLE);

                            // Save the bitmap to the Pictures directory
                            saveSnapshotToPictures(bitmap);
                        }
                    });
                }

                @Override
                public void onCancel() {
                    Toast.makeText(MapsActivity.this, "Camera animation canceled.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void saveSnapshotToPictures(Bitmap bitmap) {
        // Save to the public Pictures directory
        File picturesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MapSnapshots");
        if (!picturesDir.exists()) {
            picturesDir.mkdirs(); // Create the directory if it doesn't exist
        }

        // Create a file for the snapshot
        String fileName = "MapSnapshot_" + System.currentTimeMillis() + ".png";
        File snapshotFile = new File(picturesDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(snapshotFile)) {
            // Compress the bitmap and write to the file
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();

            // Notify the media scanner to index the file
            notifyMediaScanner(snapshotFile);

            Toast.makeText(this, "Snapshot saved to: " + snapshotFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.i(TAG,"Snapshot saved to: " + snapshotFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error saving snapshot: ", e);
            Toast.makeText(this, "Error saving snapshot", Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyMediaScanner(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
        Log.i(TAG,"Media Scanner ...");
    }

    private void initialCameraPosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            // Get the Fused Location Provider Client
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            // Get the last known location
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    // Create a LatLng object with the current location
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // Move the camera to the current location and set the zoom level
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15)); // Adjust the zoom level as needed
                } else {
                    // Handle case where location is null (e.g., location services are disabled)
                    Toast.makeText(this, "Unable to fetch current location.", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
}