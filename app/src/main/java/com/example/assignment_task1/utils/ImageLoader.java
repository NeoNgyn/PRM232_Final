package com.example.assignment_task1.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import java.io.IOException;
import java.io.InputStream;

public class ImageLoader {

    private static final String TAG = "ImageLoader";

    public static void loadImageFromAssets(Context context, String imagePath, ImageView imageView, int fallbackResource) {
        if (context == null || imageView == null) {
            Log.e(TAG, "Context or ImageView is null");
            return;
        }

        // First check if file exists with exact path
        if (!ImageHelper.fileExistsInAssets(context, imagePath)) {
            // Try to find a better matching path with different extensions
            String betterPath = ImageHelper.getBestMatchingImagePath(context,
                imagePath.replace("food_images/", "").replace(".jpg", "").replace(".png", "").replace(".webp", ""));
            Log.d(TAG, "Original path " + imagePath + " not found, trying: " + betterPath);
            imagePath = betterPath;
        }

        try {
            Log.d(TAG, "Trying to load image: " + imagePath);
            InputStream inputStream = context.getAssets().open(imagePath);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                Log.d(TAG, "Successfully loaded image: " + imagePath);
            } else {
                Log.w(TAG, "Bitmap is null for: " + imagePath);
                imageView.setImageResource(fallbackResource);
            }
            inputStream.close();
        } catch (IOException e) {
            // Nếu không tìm thấy ảnh trong assets, sử dụng drawable resource
            Log.w(TAG, "Failed to load image: " + imagePath + ", using fallback resource", e);
            imageView.setImageResource(fallbackResource);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading image: " + imagePath, e);
            imageView.setImageResource(fallbackResource);
        }
    }

    public static void loadImageFromAssetsAsync(Context context, String imagePath, ImageView imageView, int fallbackResource) {
        if (context == null || imageView == null) {
            Log.e(TAG, "Context or ImageView is null");
            return;
        }

        // Load ảnh trong background thread để tránh block UI
        new Thread(() -> {
            // First check if file exists, try to find better path
            if (!ImageHelper.fileExistsInAssets(context, imagePath)) {
                String betterPath = ImageHelper.getBestMatchingImagePath(context,
                    imagePath.replace("food_images/", "").replace(".jpg", "").replace(".png", "").replace(".webp", ""));
                Log.d(TAG, "Async: Original path " + imagePath + " not found, trying: " + betterPath);
                final String updatedPath = betterPath;

                // Continue with updated path
                loadBitmapAsync(context, updatedPath, imageView, fallbackResource);
            } else {
                // Original path exists, proceed
                loadBitmapAsync(context, imagePath, imageView, fallbackResource);
            }
        }).start();
    }

    private static void loadBitmapAsync(Context context, String imagePath, ImageView imageView, int fallbackResource) {
        try {
            Log.d(TAG, "Async loading image: " + imagePath);
            InputStream inputStream = context.getAssets().open(imagePath);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Switch back to main thread to update UI
            imageView.post(() -> {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    Log.d(TAG, "Successfully loaded async image: " + imagePath);
                } else {
                    imageView.setImageResource(fallbackResource);
                    Log.w(TAG, "Async bitmap is null for: " + imagePath);
                }
            });
            inputStream.close();
        } catch (IOException e) {
            // Fallback to drawable resource on main thread
            Log.w(TAG, "Failed to load async image: " + imagePath + ", using fallback", e);
            imageView.post(() -> imageView.setImageResource(fallbackResource));
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading async image: " + imagePath, e);
            imageView.post(() -> imageView.setImageResource(fallbackResource));
        }
    }
}
