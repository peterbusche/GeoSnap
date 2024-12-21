/*
THIS NEEDS TO BE IMPLEMENTED
    -TBD: if this will be a seperate activity, or if this will be integrated in MapActivity.java
    -still needs to be tested. should be developed seperately
        -need to create a test that provides changing cell data to trigger interupt
        -will figure out some output to test how well it works
    -Will implement this after I create database and Ruby API?
 */

package com.example.mapapp2;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TelephonyManager
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        // Check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            setupPhoneStateListener();
        }
    }

    private void setupPhoneStateListener() {
        // Define PhoneStateListener
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);

                // Get signal strength level
                int level = signalStrength.getLevel(); // 0 to 4
                Log.d("SignalStrength", "Signal strength level: " + level);

                // Trigger logic when signal strength is low
                if (level <= 1) {
                    Log.d("SignalStrength", "Signal strength below threshold. Triggering action.");
                    // Add your snapshot logic here
                }
            }
        };

        // Register listener
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister listener to avoid memory leaks
        if (telephonyManager != null && phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupPhoneStateListener();
        } else {
            Log.e("Permissions", "Permissions denied. Unable to monitor signal strength.");
        }
    }
}
