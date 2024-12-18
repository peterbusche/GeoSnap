package com.example.mapapp2;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;



import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapboxMap;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
//import com.mapbox.maps.plugin.annotation.annotations;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    //MISC
    private static String TAG = "MyAppLogs";
    private Handler handler;


    //MAPS and DATA
    private MapView mapView;
    private MapboxMap mapboxMap;
    private TextView latLongTextView;


    // UI Components
    private Button zoomInButton, zoomOutButton;


    //tracking data variables
    ArrayList<Point> trackedLocations = new ArrayList<Point>();
    private boolean useSimulatedData = false;
    private int simulatedIndex=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize MapView

        mapView = findViewById(R.id.mapView);


        // Zoom Buttons
        zoomInButton = findViewById(R.id.zoom_in_button);
        zoomOutButton = findViewById(R.id.zoom_out_button);


        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            mapboxMap = mapView.getMapboxMap();
            setInitialCameraPosition();

//                if(useSimulatedData) {
//                    //initializeSimulatedLocations();
//                    //startSimulatedTracking();
//                } else {
//                    //checkAndRequestPermissions();
//                }
        });


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

    private void initializeSimulatedLocations() {
        trackedLocations.add(Point.fromLngLat(-116.2023, 43.6150)); // Boise
        trackedLocations.add(Point.fromLngLat(-116.2038, 43.6165)); // Nearby location 1
        trackedLocations.add(Point.fromLngLat(-116.2053, 43.6180)); // Nearby location 2
        trackedLocations.add(Point.fromLngLat(-116.2065, 43.6200)); // Nearby location 3
        trackedLocations.add(Point.fromLngLat(-116.2078, 43.6220)); // Nearby location 4
    }

        private void startSimulatedTracking() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(simulatedIndex < trackedLocations.size()) {
                    Point location = trackedLocations.get(simulatedIndex);
                    simulatedIndex++;
                    updateLocation(location);
                    handler.postDelayed(this, 5000);
                }

            }
        }, 5000);
    }



    private void updateLocation(Point location) {
        if (mapView != null) {
            // Add a marker
            mapView.getMapboxMap().getStyle(style -> {
                // Use PointAnnotationManager to add markers
                //PointAnnotationManager pointAnnotationManager = annotations.createPointAnnotationManager(mapView);

                PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                        .withPoint(location)
                        .withIconImage("marker-icon"); // Ensure you have a valid marker image added to the style

                //pointAnnotationManager.create(pointAnnotationOptions);
            });

            // Update UI
            latLongTextView.setText(String.format("Lat: %.5f, Lon: %.5f", location.latitude(), location.longitude()));

            // Move the camera
            CameraOptions cameraPosition = new CameraOptions.Builder()
                    .center(location)
                    .zoom(15.0) // Zoom level
                    .build();
            mapView.getMapboxMap().setCamera(cameraPosition);
        }
    }

    private void addPointAnnotation(Point location) {
        mapView.getMapboxMap().getStyle(style -> {
            try {
                // Load the custom marker image from assets
                Bitmap markerBitmap = BitmapFactory.decodeStream(getAssets().open("red_marker.png"));

                // Add the image to the style
                style.addImage("red-marker-icon", markerBitmap);

                // Create the PointAnnotationManager
                PointAnnotationManager pointAnnotationManager = mapView.annotations().createPointAnnotationManager();

                // Define the annotation options
                PointAnnotationOptions options = new PointAnnotationOptions()
                        .withPoint(location) // Set the location of the marker
                        .withIconImage("red-marker-icon"); // Use the custom marker

                // Add the annotation to the map
                pointAnnotationManager.create(options);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
