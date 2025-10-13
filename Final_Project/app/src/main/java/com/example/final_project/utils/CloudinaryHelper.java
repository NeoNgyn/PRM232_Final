package com.example.final_project.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Minimal Cloudinary uploader using unsigned upload preset.
 * Configure CLOUD_NAME and UPLOAD_PRESET in your local.properties or replace below.
 */
public class CloudinaryHelper {
    // Loaded from BuildConfig (populated from local.properties)
    public static final String CLOUD_NAME = com.example.final_project.BuildConfig.CLOUDINARY_CLOUD_NAME;
    public static final String UPLOAD_PRESET = com.example.final_project.BuildConfig.CLOUDINARY_UPLOAD_PRESET;

    private static final OkHttpClient client = new OkHttpClient();

    public interface UploadCallback {
        void onSuccess(String url);
        void onError(String error);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        if (imageUri == null) {
            callback.onError("imageUri is null");
            return;
        }

        try {
            InputStream is = context.getContentResolver().openInputStream(imageUri);
            if (is == null) {
                callback.onError("Unable to open image");
                return;
            }

            byte[] bytes = new byte[is.available()];
            int read = is.read(bytes);
            is.close();

            RequestBody fileBody = RequestBody.create(bytes, MediaType.parse("image/*"));

            MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);
            mb.addFormDataPart("file", "upload.jpg", fileBody);
            mb.addFormDataPart("upload_preset", UPLOAD_PRESET);

            RequestBody requestBody = mb.build();

            String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
            Request request = new Request.Builder().url(url).post(requestBody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String body = response.body() != null ? response.body().string() : "";
                        new Handler(Looper.getMainLooper()).post(() -> callback.onError("Upload failed: " + body));
                        return;
                    }
                    String body = response.body() != null ? response.body().string() : "";
                    // The response is JSON containing 'secure_url' — for simplicity, extract using a naive search
                    String secureUrl = parseSecureUrl(body);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (secureUrl != null) callback.onSuccess(secureUrl);
                        else callback.onError("No secure_url in response");
                    });
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private static String parseSecureUrl(String json) {
        // naive parsing to avoid adding a JSON dependency — looks for "secure_url":"..."
        if (json == null) return null;
        String key = "\"secure_url\":\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        int start = idx + key.length();
        int end = json.indexOf('"', start);
        if (end == -1) return null;
        return json.substring(start, end).replaceAll("\\\\/", "/");
    }
}
