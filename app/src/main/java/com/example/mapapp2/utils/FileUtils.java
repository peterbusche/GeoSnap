package com.example.mapapp2.utils;

import android.util.Log;

import java.io.File;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Check if a file exists.
     *
     * @param filePath Path to the file.
     * @return True if the file exists, false otherwise.
     */
    public static boolean doesFileExist(String filePath) {
        File file = new File(filePath);
        boolean exists = file.exists();
        Log.d(TAG, "File exists: " + exists + " for path: " + filePath);
        return exists;
    }

    /**
     * Delete a file.
     *
     * @param filePath Path to the file.
     * @return True if the file was deleted, false otherwise.
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, "File deleted: " + deleted + " for path: " + filePath);
            return deleted;
        } else {
            Log.d(TAG, "File not found for deletion: " + filePath);
            return false;
        }
    }

    /**
     * Get the size of a file in bytes.
     *
     * @param filePath Path to the file.
     * @return File size in bytes, or -1 if the file doesn't exist.
     */
    public static long getFileSize(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            long size = file.length();
            Log.d(TAG, "File size: " + size + " bytes for path: " + filePath);
            return size;
        } else {
            Log.d(TAG, "File not found to get size: " + filePath);
            return -1;
        }
    }
}
