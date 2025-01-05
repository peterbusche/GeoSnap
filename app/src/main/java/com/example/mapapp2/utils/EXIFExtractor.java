package com.example.mapapp2.utils;

import android.media.ExifInterface;
import android.util.Log;

import java.io.IOException;

public class EXIFExtractor {
    private static final String TAG = "EXIFExtractor";

    /**
     * Extract latitude and longitude from an image file.
     *
     * @param filePath The file path of the image.
     * @return A double array with latitude and longitude, or null if unavailable.
     */
    public static double[] extractLatLng(String filePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);

            float[] latLng = new float[2];
            if (exifInterface.getLatLong(latLng)) {
                Log.d(TAG, "Extracted lat/lng: " + latLng[0] + ", " + latLng[1]);
                return new double[]{latLng[0], latLng[1]};
            } else {
                Log.d(TAG, "No GPS data found in EXIF metadata for file: " + filePath);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF metadata for file: " + filePath, e);
        }

        return null;
    }
}
