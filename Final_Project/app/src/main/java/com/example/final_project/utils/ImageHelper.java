package com.example.final_project.utils;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

public class ImageHelper {

    private static final String TAG = "ImageHelper";
    private static final String ASSET_SUBFOLDER = "food_images";

    /**
     * Checks if a specific file exists in the assets folder.
     *
     * @param context   The context.
     * @param filePath  The full path to the file in the assets folder.
     * @return True if the file exists, false otherwise.
     */
    public static boolean fileExistsInAssets(Context context, String filePath) {
        if (context == null || filePath == null || filePath.isEmpty()) {
            return false;
        }
        try {
            // Attempt to open the file to see if it throws an exception.
            context.getAssets().open(filePath).close();
            return true;
        } catch (IOException e) {
            // File does not exist.
            return false;
        }
    }

    /**
     * Searches for the best-matching image file in the assets subfolder, trying different extensions.
     *
     * @param context   The context.
     * @param baseName  The base name of the image file (without extension).
     * @return The full asset path of the best-matching image, or a default fallback path if not found.
     */
    public static String getBestMatchingImagePath(Context context, String baseName) {
        if (context == null || baseName == null || baseName.trim().isEmpty()) {
            return "";
        }

        // Define the extensions to check, in order of preference.
        String[] extensions = {".jpg", ".png", ".webp"};

        for (String ext : extensions) {
            String fullPath = ASSET_SUBFOLDER + "/" + baseName.trim() + ext;
            if (fileExistsInAssets(context, fullPath)) {
                Log.d(TAG, "Found matching image at: " + fullPath);
                return fullPath; // Return the first match found.
            }
        }

        Log.w(TAG, "No matching image found for base name: " + baseName);
        // Return a default guess, which might or might not exist.
        return ASSET_SUBFOLDER + "/" + baseName + ".jpg";
    }
}
