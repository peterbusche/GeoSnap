//will replace toast messages later with this class to handle any errors


package com.example.mapapp2.utils;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;

import retrofit2.Response;

public class ErrorHandler {
    public static void handleApiError(Context context, Response<?> response) {
        if (response.errorBody() != null) {
            try {
                String errorMessage = response.errorBody().string();
                Toast.makeText(context, "API Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(context, "Error parsing API response", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Unknown API error", Toast.LENGTH_SHORT).show();
        }
    }

    public static void handleNetworkError(Context context, Throwable t) {
        Toast.makeText(context, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
