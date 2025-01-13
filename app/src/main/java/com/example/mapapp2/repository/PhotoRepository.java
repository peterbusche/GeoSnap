package com.example.mapapp2.repository;

import android.content.Context;
import android.util.Log;

import com.example.mapapp2.models.PhotoMetadata;
import com.example.mapapp2.utils.EXIFExtractor;

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
        try {
            List<PhotoMetadata> metadataList = EXIFExtractor.extractPhotoMetadata(context);
            Log.d(TAG, "Fetched " + metadataList.size() + " photos with metadata.");
            return metadataList;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching photo metadata", e);
            return null;
        }
    }


}
