package com.example.mapapp2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapboxMap;
import com.mapbox.geojson.Point;

import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MyApp2";
    private MapView mapView;
    private MapboxMap mapboxMap;

    // UI Components
    private Button zoomInButton, zoomOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Here");
        super.onCreate(savedInstanceState);

        Log.e(TAG, "Here1");
        setContentView(R.layout.activity_main);
        Log.e(TAG, "Here1.5");
        // Initialize MapView
        try {
            mapView = findViewById(R.id.mapView);
            Log.e(TAG, "Here2");

            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                try {
                    mapboxMap = mapView.getMapboxMap();
                    setInitialCameraPosition();
                    Log.e(TAG, "Here3");
                } catch (Exception e) {
                    Log.e(TAG, "Error setting the initial camera position");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing the map");
        }
        // Zoom Buttons
        zoomInButton = findViewById(R.id.zoom_in_button);
        zoomOutButton = findViewById(R.id.zoom_out_button);

        // Zoom In
        zoomInButton.setOnClickListener(v -> {
            double currentZoom = mapboxMap.getCameraState().getZoom();
            mapboxMap.setCamera(new CameraOptions.Builder().zoom(currentZoom + 1).build());
        });

        // Zoom Out
        zoomOutButton.setOnClickListener(v -> {
            double currentZoom = mapboxMap.getCameraState().getZoom();
            mapboxMap.setCamera(new CameraOptions.Builder().zoom(currentZoom - 1).build());
        });
    }

    private void setInitialCameraPosition() {
        // Set initial map location (Boise, ID)
        CameraOptions cameraPosition = new CameraOptions.Builder()
                .center(Point.fromLngLat(-116.2023, 43.6150))
                .zoom(12.0)
                .build();
        mapboxMap.setCamera(cameraPosition);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
