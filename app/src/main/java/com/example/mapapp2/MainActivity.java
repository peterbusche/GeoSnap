/*
Role of this file:
    Activity Lifecycle: Manage the lifecycle methods such as onCreate(), onStart(), and onResume().
    Logic: Respond to user interactions, such as button clicks.
    UI Control: Link XML layout files to Java code using setContentView() and manipulate
                UI elements via IDs.


Other Files in This Folder:
    You may have additional Java files for:
        Other Activities: For other screens in your app.
        Custom Classes: For specific logic, such as a helper class for calculations
                        or handling APIs
 */



package com.example.mapapp2;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


public class MainActivity extends AppCompatActivity implements LocationListener{
    private LocationManager locationManager;
    private TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textview=(TextView)findViewById(R.id.textView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //get permissions to access decive
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        //handle null location
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            onLocationChanged(location);
        } else {
            textview.setText("Unable to get location. Try again later.");
        }
    }

// Registers the LocationListener to receive updates every second (1000ms)
//      or when the device moves 1 meter.
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        }
    }

//Unregisters the LocationListener to save battery when the activity is not visible.
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double longitude=location.getLongitude();
        double latitude=location.getLatitude();
        textview.setText("Longitude:   "+longitude+"\nLatitide:  "+latitude);
    }

//Called when a location provider (e.g., GPS) is enabled. Displays a toast message indicating that the provider is available.
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Provider enabled: " + provider, Toast.LENGTH_SHORT).show();
    }

//Called when a location provider is disabled.Displays a toast message indicating that the provider is unavailable.
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Provider disabled: " + provider, Toast.LENGTH_SHORT).show();
    }
}

