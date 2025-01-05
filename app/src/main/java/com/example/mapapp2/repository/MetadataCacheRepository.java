package com.example.mapapp2.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mapapp2.models.PhotoMetadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MetadataCacheRepository {
    private static final String TAG = "MetadataCacheRepository";
    private static final String PREFS_NAME = "PhotoMetadataCache";
    private static final String METADATA_KEY = "photo_metadata";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public MetadataCacheRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Save metadata list to cache.
     *
     * @param metadataList List of PhotoMetadata
     */
    public void saveMetadataToCache(List<PhotoMetadata> metadataList) {
        String json = gson.toJson(metadataList);
        sharedPreferences.edit().putString(METADATA_KEY, json).apply();
        Log.d(TAG, "Metadata saved to cache.");
    }

    /**
     * Load metadata list from cache.
     *
     * @return List of PhotoMetadata
     */
    public List<PhotoMetadata> loadMetadataFromCache() {
        String json = sharedPreferences.getString(METADATA_KEY, null);
        if (json == null) {
            Log.d(TAG, "No metadata found in cache.");
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<PhotoMetadata>>() {}.getType();
        List<PhotoMetadata> metadataList = gson.fromJson(json, type);
        Log.d(TAG, "Metadata loaded from cache. Size: " + metadataList.size());
        return metadataList;
    }

    /**
     * Clear metadata cache.
     */
    public void clearCache() {
        sharedPreferences.edit().remove(METADATA_KEY).apply();
        Log.d(TAG, "Metadata cache cleared.");
    }
}
