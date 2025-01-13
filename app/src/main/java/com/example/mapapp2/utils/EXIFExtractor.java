package com.example.mapapp2.utils;

import com.example.mapapp2.models.PhotoMetadata;

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
import java.util.ArrayList;
import java.util.List;

public class EXIFExtractor {
    private static final String TAG = "EXIFExtractor";

    /**
     * Extract metadata including latitude and longitude from images.
     * Mediastore is setup like a database, so we just need to write a mediastore query
     *      - All images at "MediaStore.Images.Media.EXTERNAL_CONTENT_URI"  will be indexed and accessible
     *      - This means my app doesnt need to do any recursive file-walking, since it doesnt
     *              need to worry about nested sub-directories
     *
     * @param context Application context for accessing ContentResolver.
     * @return List of PhotoMetadata containing file path, latitude, longitude, and timestamp.
     */
    public static List<PhotoMetadata> extractPhotoMetadata(Context context) {
        List<PhotoMetadata> metadataList = new ArrayList<>();
        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; //collection (database table) to query

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN
        };

        ContentResolver contentResolver = context.getContentResolver(); //how we interact with mediastore

        //fetch all rows from mediaStore image collection ordered by date_taken in decending order
        try (Cursor cursor = contentResolver.query(collection, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) { //read through query result
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)); //unique id for image
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)); //filename
                    Uri fileUri = ContentUris.withAppendedId(collection, id); //construct uril for the image by appending the id to the colleciton uri

                    Log.d(TAG, "Processing file: " + displayName + " (URI: " + fileUri.toString() + ")");

                    try (InputStream inputStream = contentResolver.openInputStream(fileUri)) { //open inputstream to read image file
                        //create metadata object to interact with drew noakes library
                        Metadata metadata = ImageMetadataReader.readMetadata(inputStream);

                        // Extract latitude and longitude
                        double[] latLng = extractLatLng(metadata);


                        if (latLng != null) {
                            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                            PhotoMetadata photoMetadata = new PhotoMetadata(fileUri.toString(), latLng[0], latLng[1], timestamp); //create our PhotoMetadata object with data
                            metadataList.add(photoMetadata); //add this object to our list which we will eventually return to our MainActivity.java class
                            Log.d(TAG, "Added PhotoMetadata: " + photoMetadata);
                        } else {
                            Log.d(TAG, "No GPS data found for file: " + displayName);
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

        return metadataList; //return our constructed list of PhotoMetadata objects
    }


    //use drew noakes library to grab lat/lang from raw metadata
    private static double[] extractLatLng(Metadata metadata) {
        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
            return new double[]{
                    gpsDirectory.getGeoLocation().getLatitude(),
                    gpsDirectory.getGeoLocation().getLongitude()
            };
        }
        return null;
    }
}
