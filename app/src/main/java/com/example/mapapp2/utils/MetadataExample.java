package com.example.mapapp2.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.GpsDirectory;

import java.io.InputStream;

public class MetadataExample {
    private static final String TAG = "MetadataExample";

    /**
     * Extract GPS metadata from images in MediaStore.
     *
     * @param context Application context for accessing ContentResolver.
     */
    public static void extractMetadataFromMediaStore(Context context) {
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN
        };

        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(collection, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    Uri fileUri = ContentUris.withAppendedId(collection, id);

                    Log.d(TAG, "Processing file: " + displayName);

                    try (InputStream inputStream = contentResolver.openInputStream(fileUri)) {
                        Metadata metadata = ImageMetadataReader.readMetadata(inputStream);

                        // Check for the camera model
                        String cameraModel = null;
                        for (Directory directory : metadata.getDirectories()) {
                            if (directory.containsTag(ExifDirectoryBase.TAG_MODEL)) {
                                cameraModel = directory.getString(ExifDirectoryBase.TAG_MODEL);
                                break;
                            }
                        }

                        if (cameraModel != null) {
                            Log.d(TAG, "Camera Model: " + cameraModel);
                        } else {
                            Log.d(TAG, "No camera model metadata found in: " + displayName);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading metadata for file: " + displayName, e);
                    }
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No images found in MediaStore.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying MediaStore", e);
        }
    }

}
