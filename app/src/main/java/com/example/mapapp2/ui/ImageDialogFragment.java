package com.example.mapapp2.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mapapp2.R;
import com.example.mapapp2.models.PhotoMetadata;

import java.util.List;

public class ImageDialogFragment extends DialogFragment {

    private static final String ARG_IMAGE_LIST = "image_list";

    public static ImageDialogFragment newInstance(List<PhotoMetadata> imageList) {
        ImageDialogFragment fragment = new ImageDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGE_LIST, (java.io.Serializable) imageList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_dialog, container, false);

        // Set the dialog to take up more screen space
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Close button logic
        ImageView closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dismiss());

        // Get the list of images
        List<PhotoMetadata> imageList = (List<PhotoMetadata>) getArguments().getSerializable(ARG_IMAGE_LIST);

        // Scrollable horizontal layout
        LinearLayout imageContainer = view.findViewById(R.id.image_container);

        if (imageList != null) {
            for (PhotoMetadata metadata : imageList) {
                // Create an ImageView for each photo
                ImageView imageView = new ImageView(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500, 500);
                layoutParams.setMargins(16, 0, 16, 0); // Add left and right margins
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // Load the image using its URI
                imageView.setImageURI(android.net.Uri.parse(metadata.getFilePath()));
                imageContainer.addView(imageView);
            }
        }

        return view;
    }


}
