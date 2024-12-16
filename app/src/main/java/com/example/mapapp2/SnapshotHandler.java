package com.example.mapapp2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.Style;
import org.maplibre.android.snapshotter.MapSnapshot;
import org.maplibre.android.snapshotter.MapSnapshotter;
import org.maplibre.android.style.expressions.Expression;
import org.maplibre.android.style.layers.HeatmapLayer;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.android.style.sources.Source;

import java.net.URI;
import java.net.URISyntaxException;

import android.widget.FrameLayout;
import android.widget.ImageView;


public class SnapshotHandler extends AppCompatActivity implements MapSnapshotter.SnapshotReadyCallback {

    private static final String TAG = "MapSnapshotterHeatMap";
    private static final String EARTHQUAKE_SOURCE_URL = "https://maplibre.org/maplibre-gl-js/docs/assets/earthquakes.geojson";
    private static final String EARTHQUAKE_SOURCE_ID = "earthquakes";
    private static final String HEATMAP_LAYER_ID = "earthquakes-heat";
    private static final String HEATMAP_LAYER_SOURCE = "earthquakes";

    private MapSnapshotter mapSnapshotter;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_map_snapshotter_marker);
//        View container = findViewById(R.id.container);
        setContentView(R.layout.activity_main);
        FrameLayout snapshotContainer = findViewById(R.id.snapshot_container);

        // Listen for layout completion before initializing the snapshotter
        snapshotContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                snapshotContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Log.i(TAG, "Starting snapshot");

                // Build style for the snapshot
                Style.Builder builder = new Style.Builder()
                        .fromUri("mapbox://styles/mapbox/americana-v11") // Replace with your desired style
                        .withSource(getEarthquakeSource())
                        .withLayerAbove(getHeatmapLayer(), "water");

                // Configure snapshot options
                MapSnapshotter.Options options = new MapSnapshotter.Options(snapshotContainer.getMeasuredWidth(), snapshotContainer.getMeasuredHeight())
                        .withStyleBuilder(builder)
                        .withCameraPosition(new CameraPosition.Builder()
                                .target(new LatLng(15.0, -94.0))
                                .zoom(5.0)
                                .padding(1.0, 1.0, 1.0, 1.0)
                                .build());

                // Initialize and start the snapshotter
                mapSnapshotter = new MapSnapshotter(SnapshotHandler.this, options);
                mapSnapshotter.start(SnapshotHandler.this);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onSnapshotReady(MapSnapshot snapshot) {
        Log.i(TAG, "Snapshot ready");

        // Display the snapshot in an ImageView
//        ImageView imageView = findViewById(R.id.snapshot_image);
        ImageView snapshotImageView = findViewById(R.id.snapshotImageView);
        if (snapshot != null && snapshot.getBitmap() != null) {
            Bitmap bitmap = snapshot.getBitmap();
            snapshotImageView.setImageBitmap(bitmap);
        } else {
            Log.e(TAG, "Snapshot is null or failed to generate.");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapSnapshotter != null) {
            mapSnapshotter.cancel();
        }
    }

    // Helper method to create a heatmap layer
    private HeatmapLayer getHeatmapLayer() {
        HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, EARTHQUAKE_SOURCE_ID);
        layer.setMaxZoom(9f);
        layer.setSourceLayer(HEATMAP_LAYER_SOURCE);

        layer.setProperties(
                PropertyFactory.heatmapColor(
                        Expression.interpolate(
                                Expression.linear(), Expression.heatmapDensity(),
                                Expression.literal(0), Expression.rgba(33, 102, 172, 0),
                                Expression.literal(0.2), Expression.rgb(103, 169, 207),
                                Expression.literal(0.4), Expression.rgb(209, 229, 240),
                                Expression.literal(0.6), Expression.rgb(253, 219, 199),
                                Expression.literal(0.8), Expression.rgb(239, 138, 98),
                                Expression.literal(1), Expression.rgb(178, 24, 43)
                        )
                ),
                PropertyFactory.heatmapWeight(
                        Expression.interpolate(
                                Expression.linear(),
                                Expression.get("mag"),
                                Expression.stop(0, 0),
                                Expression.stop(6, 1)
                        )
                ),
                PropertyFactory.heatmapIntensity(
                        Expression.interpolate(
                                Expression.linear(),
                                Expression.zoom(),
                                Expression.stop(0, 1),
                                Expression.stop(9, 3)
                        )
                ),
                PropertyFactory.heatmapRadius(
                        Expression.interpolate(
                                Expression.linear(),
                                Expression.zoom(),
                                Expression.stop(0, 2),
                                Expression.stop(9, 20)
                        )
                ),
                PropertyFactory.heatmapOpacity(
                        Expression.interpolate(
                                Expression.linear(),
                                Expression.zoom(),
                                Expression.stop(7, 1),
                                Expression.stop(9, 0)
                        )
                )
        );
        return layer;
    }

    // Helper method to create a GeoJson source for earthquakes
    private Source getEarthquakeSource() {
        try {
            return new GeoJsonSource(EARTHQUAKE_SOURCE_ID, new URI(EARTHQUAKE_SOURCE_URL));
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid GeoJson source URI", e);
            return null;
        }
    }
}