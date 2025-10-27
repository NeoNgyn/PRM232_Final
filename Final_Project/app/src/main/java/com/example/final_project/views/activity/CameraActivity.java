package com.example.final_project.views.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.final_project.R;
import com.example.final_project.models.dto.LogMealResponse;
import com.example.final_project.network.FoodApiService;
import com.example.final_project.utils.FileUtils;
import com.example.final_project.utils.FoodApiClient;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button btnCapture;
    private ImageButton btnFlipCamera;
    private ImageButton btnBack;
    private ImageView ivPreviewImage;
    private TextView tvStatus;
    private TextView tvCounter;

    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private int photoCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initViews();
        setupClickListeners();

        // Kiểm tra và yêu cầu quyền camera
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void initViews() {
        previewView = findViewById(R.id.previewView);
        btnCapture = findViewById(R.id.btnCapture);
        btnFlipCamera = findViewById(R.id.btnFlipCamera);
        btnBack = findViewById(R.id.btnBack);
        ivPreviewImage = findViewById(R.id.ivPreviewImage);
        tvStatus = findViewById(R.id.tvStatus);
        tvCounter = findViewById(R.id.tvCounter);

        updatePhotoCounter();
    }

    private void setupClickListeners() {
        // Nút chụp ảnh
        btnCapture.setOnClickListener(v -> takePhoto());

        // Nút đổi camera (trước/sau)
        btnFlipCamera.setOnClickListener(v -> flipCamera());

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Click vào preview để xem ảnh lớn hơn
        ivPreviewImage.setOnClickListener(v -> {
            // Có thể mở ảnh trong một activity khác hoặc dialog
            Toast.makeText(this, "Xem ảnh đã chụp", Toast.LENGTH_SHORT).show();
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
            ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
                tvStatus.setText("Sẵn sàng chụp");
            } catch (ExecutionException | InterruptedException e) {
                tvStatus.setText("Lỗi khởi tạo camera: " + e.getMessage());
                Toast.makeText(this, "Không thể khởi động camera", Toast.LENGTH_SHORT).show();
            }
        }, getExecutor());
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // ImageCapture
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        // Unbind trước khi bind lại
        cameraProvider.unbindAll();

        // Bind vào lifecycle
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo tên file với timestamp
        String name = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FinalProject");

        // Tạo output options
        ImageCapture.OutputFileOptions outputOptions =
            new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build();

        // Disable controls để tránh spam
        setControlsEnabled(false);
        tvStatus.setText("Đang chụp...");

        // Chụp ảnh
        imageCapture.takePicture(
            outputOptions,
            getExecutor(),
            new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    runOnUiThread(() -> {
                        setControlsEnabled(true);
                        photoCount++;
                        updatePhotoCounter();

                        String msg = "✓ Đã lưu: " + name + ".jpg";
                        Toast.makeText(CameraActivity.this, msg, Toast.LENGTH_SHORT).show();
                        tvStatus.setText("Chụp thành công!");

                        // Hiển thị ảnh vừa chụp
                        if (outputFileResults.getSavedUri() != null) {
                            ivPreviewImage.setImageURI(outputFileResults.getSavedUri());
                            ivPreviewImage.setVisibility(View.VISIBLE);
                        }

                        analyzeImage(outputFileResults.getSavedUri());
                    });
                }



                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    runOnUiThread(() -> {
                        setControlsEnabled(true);
                        String msg = "✗ Lỗi: " + exception.getMessage();
                        Toast.makeText(CameraActivity.this, msg, Toast.LENGTH_LONG).show();
                        tvStatus.setText("Lỗi chụp ảnh");
                    });
                }
            }
        );
    }

    private void flipCamera() {
        // Đổi giữa camera trước và sau
        if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
            Toast.makeText(this, "Chuyển sang camera trước", Toast.LENGTH_SHORT).show();
        } else {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
            Toast.makeText(this, "Chuyển sang camera sau", Toast.LENGTH_SHORT).show();
        }

        // Khởi động lại camera với selector mới
        startCamera();
    }

    private void setControlsEnabled(boolean enabled) {
        btnCapture.setEnabled(enabled);
        btnFlipCamera.setEnabled(enabled);
    }

    private void updatePhotoCounter() {
        tvCounter.setText("Đã chụp: " + photoCount + " ảnh");
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                    "Cần cấp quyền camera để sử dụng chức năng này",
                    Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void analyzeImage(Uri imageUri) {
        tvStatus.setText("🔍 Đang phân tích món ăn...");

        new Thread(() -> {
            try {
                // Lấy file từ URI
                File file = compressImage(imageUri);

                RequestBody requestBody = RequestBody.create(
                        okhttp3.MediaType.parse("image/jpeg"), file);
                MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                        "image", file.getName(), requestBody);

                // Tạo retrofit instance
                FoodApiService apiService = FoodApiClient.getClient().create(FoodApiService.class);
                String token = "Bearer 9d885ef142b2caf972e4c5fa754624c44702aaae"; // ⚠️ Thay bằng token thật

                // Gọi API
                Call<LogMealResponse> call = apiService.identifyFood(token, imagePart);
                Response<LogMealResponse> response = call.execute();

                runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        LogMealResponse logMeal = response.body();
                        if (logMeal.getRecognition_results() != null && !logMeal.getRecognition_results().isEmpty()) {
                            String name = logMeal.getRecognition_results().get(0).getName();
                            double prob = logMeal.getRecognition_results().get(0).getProbability();

                            Intent intent = new Intent(CameraActivity.this, FoodResultActivity.class);
                            intent.putExtra("imageUri", imageUri.toString());
                            intent.putExtra("foodName", name);
                            intent.putExtra("confidence", logMeal.getRecognition_results().get(0).getProbability() * 100);
                            startActivity(intent);
                        } else {
                            tvStatus.setText("❌ Không nhận diện được món ăn.");
                        }
                    } else {
                        tvStatus.setText("❌ Phân tích thất bại: " + response.code());
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("Lỗi: " + e.getMessage()));
            }
        }).start();
    }

    private File compressImage(Uri imageUri) throws IOException {
        // Lấy bitmap từ URI
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

        // Tạo file tạm trong cache
        File compressedFile = new File(getCacheDir(), "compressed_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(compressedFile);

        // Nén ảnh xuống ~70% chất lượng (thường còn 1–2 MB)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
        fos.close();

        return compressedFile;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup nếu cần
    }
}

