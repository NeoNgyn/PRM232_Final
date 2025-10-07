package com.example.final_project.views.fragment;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.final_project.R;
import com.google.common.util.concurrent.ListenableFuture;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class ScanFragment extends Fragment {
    private static final int REQUEST_CAMERA_PERMISSION = 10;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button btnCapture;
    private ImageView ivCapturedImage;
    private TextView tvStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // Khởi tạo views
        previewView = view.findViewById(R.id.previewView);
        btnCapture = view.findViewById(R.id.btnCapture);
        ivCapturedImage = view.findViewById(R.id.ivCapturedImage);
        tvStatus = view.findViewById(R.id.tvStatus);

        // Xử lý sự kiện click nút chụp
        btnCapture.setOnClickListener(v -> takePhoto());

        // Kiểm tra quyền camera
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        return view;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // ImageCapture - để chụp ảnh
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                // Chọn camera sau
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind các use case trước khi bind lại
                cameraProvider.unbindAll();

                // Bind cả preview và imageCapture
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                tvStatus.setText("Sẵn sàng chụp");

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                tvStatus.setText("Lỗi khởi tạo camera");
            }
        }, getExecutor());
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(requireContext(), "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo tên file với timestamp
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FinalProject");

        // Tạo output options
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                requireContext().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        // Disable nút chụp để tránh chụp nhiều lần
        btnCapture.setEnabled(false);
        tvStatus.setText("Đang chụp...");

        // Chụp ảnh
        imageCapture.takePicture(
                outputOptions,
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        requireActivity().runOnUiThread(() -> {
                            btnCapture.setEnabled(true);
                            String msg = "Đã lưu ảnh: " + name;
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            tvStatus.setText("Chụp thành công!");

                            // Hiển thị ảnh vừa chụp
                            if (outputFileResults.getSavedUri() != null) {
                                ivCapturedImage.setImageURI(outputFileResults.getSavedUri());
                                ivCapturedImage.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        requireActivity().runOnUiThread(() -> {
                            btnCapture.setEnabled(true);
                            String msg = "Lỗi chụp ảnh: " + exception.getMessage();
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            tvStatus.setText("Lỗi chụp ảnh");
                        });
                    }
                }
        );
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(requireContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(requireContext(), "Cần quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
            tvStatus.setText("Không có quyền camera");
        }
    }
}
