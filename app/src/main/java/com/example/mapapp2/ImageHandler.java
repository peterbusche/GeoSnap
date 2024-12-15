package com.example.mapapp2;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class ImageHandler {
    private static final String TAG = "ImageHandler";

    private final Context context;

    public ImageHandler(Context context) {
        this.context = context;
    }

    public void displayImage(String imageName, FrameLayout imageContainer, ImageView imageImageView) {
        try {
            // Load the image from the assets folder
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Set the image in the ImageView
            imageImageView.setImageBitmap(bitmap);

            // Show the image container
            imageContainer.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load image from assets: " + imageName, e);
        }
    }

    public void closeImage(FrameLayout imageContainer, ImageView imageImageView) {
        // Hide the image container and clear the bitmap
        imageContainer.setVisibility(View.GONE);
        imageImageView.setImageBitmap(null);
    }
}

