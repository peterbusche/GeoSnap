package com.example.mapapp2;

import com.example.mapapp2.BuildConfig;

import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.appcompat.app.AppCompatActivity;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;




public class MainActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the API Key from BuildConfig
        String apiKey = BuildConfig.MAPTILER_API_KEY;

        // Find other maps in https://cloud.maptiler.com/maps/
        String mapId = "streets-v2";

        String styleUrl = "https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + apiKey;

        // Initialize MapLibre
        MapLibre.getInstance(this);

        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(this);
        setContentView(inflater.inflate(R.layout.activity_main, null));

        // Initialize the MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        // Set up the map with the desired style and camera position
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapLibreMap mapLibreMap) {
                mapLibreMap.setStyle(styleUrl);

                // Set initial camera position
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(0.0, 0.0) ) // Default location
                        .zoom(0.0)                   // Default zoom
                        .build();

                mapLibreMap.setCameraPosition(position);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
}

