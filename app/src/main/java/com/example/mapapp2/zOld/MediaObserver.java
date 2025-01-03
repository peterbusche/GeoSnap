package com.example.mapapp2.zOld;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

public class MediaObserver extends ContentObserver {

    private ContentResolver contentResolver;
    private OnNewPhotoListener listener;

    public interface OnNewPhotoListener {
        void onNewPhoto(Uri photoUri);
    }

    public MediaObserver(Handler handler, ContentResolver contentResolver, OnNewPhotoListener listener) {
        super(handler);
        this.contentResolver = contentResolver;
        this.listener = listener;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        // Query the most recent photo in MediaStore
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String photoPath = cursor.getString(dataColumn);
            Uri photoUri = Uri.parse(photoPath);
            listener.onNewPhoto(photoUri);
            cursor.close();
        }
    }
}

