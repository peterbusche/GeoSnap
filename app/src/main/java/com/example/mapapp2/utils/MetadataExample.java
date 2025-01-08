/*
Followed this to get past external storage issues:
https://stackoverflow.com/questions/62782648/android-11-scoped-storage-permissions

URI - uniform resource identifier
    -In this example, they point to the locations where our media files are indexed by media store
    -URL is a type of URI. URN is a type of URI.
 */


package com.example.mapapp2.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;

import java.io.InputStream;

public class MetadataExample {

    private static final String TAG = "MetadataExample";

    public static void extractMetadataFromMediaStore(Context context) {
        Uri[] collections;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //for android 11
            Log.i(TAG, "Route for Android 11");
            collections = new Uri[]{
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), //check primary external storage (
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI //check specifically downloads
            };
        } else { //for android 10 and below
            Log.i(TAG, "Route for Android 10");
            collections = new Uri[]{MediaStore.Images.Media.EXTERNAL_CONTENT_URI};
        }

        for (Uri collection : collections) {
            queryMediaStore(context, collection);
        }
    }

    private static void queryMediaStore(Context context, Uri collection) {
        //define data we want to query
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN
        };

        //get our app context to interact with mediastore
        ContentResolver contentResolver = context.getContentResolver();

        //check if we are allowed to create the cursor
        //cursor is given our URIs for storage, data we want to select, and primary key with direciton to select data
        try (Cursor cursor = contentResolver.query(collection, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC")) {
            //start iterating through mediastore by row
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    //find our file and get its info so we can access it
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    //remember, collection is just a row from our collections array, so its just a string?
                    Uri fileUri = ContentUris.withAppendedId(collection, id); //constructs URI for this file by appending id to the URI collection we created

                    Log.d(TAG, "Processing file: " + fileUri.toString());

                    try (InputStream inputStream = contentResolver.openInputStream(fileUri)) { //open a stream to read binary content of the file represented by fileUri

                        /*
                        ========= WHERE DREW NOAKES LIBRARY INTERACTS WITH METADATA ====================
                        -now we can read the EXIF metadata from the stream
                        -next, we will parse the metadata into a collection of Directory objects where each Directory contains specific tags distinguishing the data
                        */
                        Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
                        String cameraModel = null;
                        for (Directory directory : metadata.getDirectories()) {  //
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
                Log.d(TAG, "No images found in MediaStore for: " + collection);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying MediaStore", e);
        }
        Log.i(TAG, "End of mediastore query");

    }
}