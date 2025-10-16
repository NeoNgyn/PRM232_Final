package com.example.final_project.views.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_project.R;
import com.example.final_project.models.entity.Menu;
import com.example.final_project.utils.DatabaseConnection;
import com.example.final_project.utils.CloudinaryHelper;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

public class CreateMenuActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText etMenuName, etMenuDescription;
    private ImageView imageMenu;
    private Uri imageUri;

    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_menu);

        etMenuName = findViewById(R.id.etMenuName);
        etMenuDescription = findViewById(R.id.etMenuDescription);
        imageMenu = findViewById(R.id.imageMenu);
        Button btnUploadImage = findViewById(R.id.btnUploadImage);
        Button btnSaveMenu = findViewById(R.id.btnSaveMenu);

        btnUploadImage.setOnClickListener(v -> openImagePicker());
        btnSaveMenu.setOnClickListener(v -> saveMenu());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageMenu.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveMenu() {
        String name = etMenuName.getText().toString().trim();
        String description = etMenuDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Runnable dbSuccessAction = () -> {
            Toast.makeText(this, "Menu '" + name + "' created successfully!", Toast.LENGTH_SHORT).show();
            finish();
        };

        Consumer<String> dbErrorAction = error -> Toast.makeText(this, "Error saving menu: " + error, Toast.LENGTH_LONG).show();

        Menu menuToSave = new Menu(null, name, null, description, null, null, null, null);

        if (imageUri != null) {
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
            CloudinaryHelper.uploadImage(this, imageUri, new CloudinaryHelper.UploadCallback() {
                @Override
                public void onSuccess(String newImageUrl) {
                    menuToSave.setImageUrl(newImageUrl);
                    insertMenuToDb(menuToSave, dbSuccessAction, dbErrorAction);
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(CreateMenuActivity.this, "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
                    // Still save menu without image
                    insertMenuToDb(menuToSave, dbSuccessAction, dbErrorAction);
                }
            });
        } else {
            menuToSave.setImageUrl(""); // Default empty image URL
            insertMenuToDb(menuToSave, dbSuccessAction, dbErrorAction);
        }
    }

    private void insertMenuToDb(Menu menu, Runnable onSuccess, Consumer<String> onError) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String sql = "INSERT INTO Menu (menu_id, menu_name, image_url, description, create_at, update_at) VALUES (?,?,?,?,?,?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    String id = menu.getMenuId();
                    if (id == null || id.isEmpty()) {
                        id = "M" + String.format("%09d", System.currentTimeMillis() % 1000000000L);
                    }
                    if (id.length() > 10) id = id.substring(0, 10);

                    stmt.setString(1, id);
                    stmt.setString(2, menu.getMenuName());
                    stmt.setString(3, menu.getImageUrl());
                    stmt.setString(4, menu.getDescription());
                    stmt.setTimestamp(5, new java.sql.Timestamp(new Date().getTime()));
                    stmt.setTimestamp(6, new java.sql.Timestamp(new Date().getTime()));

                    if (stmt.executeUpdate() <= 0) throw new SQLException("Insert failed");
                    menu.setMenuId(id);
                    runOnUiThread(onSuccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> onError.accept(e.getMessage()));
            }
        });
    }
}
