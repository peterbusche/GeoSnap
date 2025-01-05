package com.example.mapapp2.repository;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.example.mapapp2.models.PhotoMetadata;
import com.example.mapapp2.utils.EXIFExtractor;

import java.util.ArrayList;
import java.util.List;

public class PhotoRepository {
    private static final String TAG = "PhotoRepository";
    private final Context context;

    public PhotoRepository(Context context) {
        this.context = context;
    }

    /**
     * Fetch photo metadata from the user's photo library.
     *
     * @return List of PhotoMetadata
     */
    public List<PhotoMetadata> fetchPhotoMetadata() {
        List<PhotoMetadata> metadataList = new ArrayList<>();


        // Query the MediaStore for images
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DATE_TAKEN
                },
                null,
                null,
                MediaStore.Images.Media.DATE_TAKEN + " DESC"
        );


        if (cursor != null) {
            try {
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);

                while (cursor.moveToNext()) {
                    String filePath = cursor.getString(dataColumn);
                    long timestamp = cursor.getLong(dateColumn);

                    // Extract latitude and longitude using EXIFExtractor
                    double[] latLng = EXIFExtractor.extractLatLng(filePath);

                    if (latLng != null) {
                        PhotoMetadata metadata = new PhotoMetadata(
                                filePath,
                                latLng[0], // Latitude
                                latLng[1], // Longitude
                                timestamp
                        );
                        metadataList.add(metadata);
                        Log.d(TAG, "PhotoMetadata added: " + metadata);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching photo metadata", e);
            } finally {
                cursor.close();
            }
        } else {
            Log.e(TAG, "Cursor is null. Unable to fetch photos.");
        }

        return metadataList;
    }
}
