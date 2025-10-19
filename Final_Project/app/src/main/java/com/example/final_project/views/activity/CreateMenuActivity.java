package com.example.final_project.views.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_project.R;
import com.example.final_project.models.entity.Menu;
import com.example.final_project.utils.DatabaseConnection;
import com.example.final_project.utils.CloudinaryHelper;
import com.example.final_project.utils.DBMigrationHelper;
import com.bumptech.glide.Glide;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import android.app.DatePickerDialog;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class CreateMenuActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText etMenuName, etMenuDescription, etFromDate, etToDate;
    private ImageView imageMenu;
    private Uri imageUri;
    private String existingMenuId = null;
    private String existingImageUrl = null;
    private boolean isEditMode = false;
    private static final String TAG = "CreateMenuActivity";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_menu);

        etMenuName = findViewById(R.id.etMenuName);
        etMenuDescription = findViewById(R.id.etMenuDescription);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        imageMenu = findViewById(R.id.imageMenu);
        Button btnUploadImage = findViewById(R.id.btnUploadImage);
        Button btnSaveMenu = findViewById(R.id.btnSaveMenu);
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnAddRecipe = findViewById(R.id.btnAddRecipe);

        btnUploadImage.setOnClickListener(v -> openImagePicker());
        btnSaveMenu.setOnClickListener(v -> saveMenu());
        btnBack.setOnClickListener(v -> finish());
        btnAddRecipe.setOnClickListener(v -> {
            // Only allow adding a recipe if we are editing an existing (saved) menu
            if (isEditMode && existingMenuId != null && !existingMenuId.isEmpty()) {
                Intent intent = new Intent(CreateMenuActivity.this, CreateRecipeActivity.class);
                intent.putExtra("menu_id", existingMenuId);
                startActivity(intent);
            } else {
                Toast.makeText(CreateMenuActivity.this, "Please save the menu first before adding recipes.", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup date pickers
        etFromDate.setOnClickListener(v -> showDatePicker(etFromDate, "Select From Date"));
        etToDate.setOnClickListener(v -> showDatePicker(etToDate, "Select To Date"));

        // Check if editing existing menu
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("menu_id")) {
            existingMenuId = intent.getStringExtra("menu_id");
            isEditMode = true;
            loadMenuData(existingMenuId);
        }

        // Run normalization in background at startup to fix any existing long menu_ids
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null) {
                    int changed = DBMigrationHelper.normalizeMenuIds(conn);
                    if (changed > 0) Log.i(TAG, "Normalized existing menu_ids on startup: changed=" + changed);
                }
            } catch (Exception e) {
                Log.e(TAG, "Normalization on startup failed", e);
            }
        });
    }

    private void loadMenuData(String menuId) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String sql = "SELECT menu_name, image_url, description, from_date, to_date FROM Menu WHERE menu_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, menuId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String name = rs.getString("menu_name");
                            String imageUrl = rs.getString("image_url");
                            String description = rs.getString("description");
                            java.sql.Date fromDateSql = rs.getDate("from_date");
                            java.sql.Date toDateSql = rs.getDate("to_date");

                            existingImageUrl = imageUrl;

                            runOnUiThread(() -> {
                                etMenuName.setText(name);
                                etMenuDescription.setText(description);
                                
                                if (fromDateSql != null) {
                                    etFromDate.setText(dateFormat.format(new Date(fromDateSql.getTime())));
                                }
                                if (toDateSql != null) {
                                    etToDate.setText(dateFormat.format(new Date(toDateSql.getTime())));
                                }
                                
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    try {
                                        if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                                            android.net.Uri uri = android.net.Uri.parse(imageUrl);
                                            Glide.with(CreateMenuActivity.this)
                                                    .load(uri)
                                                    .placeholder(R.drawable.ic_launcher_background)
                                                    .error(R.drawable.ic_launcher_background)
                                                    .into(imageMenu);
                                        } else {
                                            Glide.with(CreateMenuActivity.this)
                                                    .load(imageUrl)
                                                    .placeholder(R.drawable.ic_launcher_background)
                                                    .error(R.drawable.ic_launcher_background)
                                                    .into(imageMenu);
                                        }
                                    } catch (Exception ex) {
                                        imageMenu.setImageResource(R.drawable.ic_launcher_background);
                                    }
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading menu data", e);
                runOnUiThread(() -> Toast.makeText(this, "Error loading menu data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showDatePicker(EditText editText, String title) {
        Calendar calendar = Calendar.getInstance();
        
        // If field already has a date, start from that date
        String currentText = editText.getText().toString();
        if (!currentText.isEmpty()) {
            try {
                Date date = dateFormat.parse(currentText);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (ParseException e) {
                Log.e(TAG, "Date parse error", e);
            }
        }
        
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    editText.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        // Use provided title to avoid unused parameter warning
        if (title != null && !title.isEmpty()) dialog.setTitle(title);
        dialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        // Use ACTION_OPEN_DOCUMENT so we can take persistable permission for the chosen Uri
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        // Grant temporary read permission and request persistable permission
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                // Persist read permission so the app can read this URI later (important for content:// URIs)
                try {
                    final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                } catch (SecurityException se) {
                    Log.w(TAG, "Could not take persistable uri permission", se);
                }

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageMenu.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e(TAG, "Error loading picked image", e);
            }
        }
    }

    private void saveMenu() {
        String name = etMenuName.getText().toString().trim();
        String description = etMenuDescription.getText().toString().trim();
        String fromDateStr = etFromDate.getText().toString().trim();
        String toDateStr = etToDate.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date range if both dates are provided
        if (!fromDateStr.isEmpty() && !toDateStr.isEmpty()) {
            try {
                Date fromDate = dateFormat.parse(fromDateStr);
                Date toDate = dateFormat.parse(toDateStr);
                if (fromDate != null && toDate != null && fromDate.after(toDate)) {
                    Toast.makeText(this, "From date must be before To date", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String successMessage = isEditMode ? "Menu '" + name + "' updated successfully!" : "Menu '" + name + "' created successfully!";
        
        Runnable dbSuccessAction = () -> {
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
            finish();
        };

        Consumer<String> dbErrorAction = error -> Toast.makeText(this, "Error saving menu: " + error, Toast.LENGTH_LONG).show();

        // Parse dates
        Date fromDate = null;
        Date toDate = null;
        try {
            if (!fromDateStr.isEmpty()) {
                fromDate = dateFormat.parse(fromDateStr);
            }
            if (!toDateStr.isEmpty()) {
                toDate = dateFormat.parse(toDateStr);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Date parse error", e);
        }

        Menu menuToSave = new Menu(existingMenuId, name, null, description, fromDate, toDate, null, null);

        if (imageUri != null) {
            // New image selected, upload it
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
            CloudinaryHelper.uploadImage(this, imageUri, new CloudinaryHelper.UploadCallback() {
                @Override
                public void onSuccess(String newImageUrl) {
                    menuToSave.setImageUrl(newImageUrl);
                    if (isEditMode) {
                        updateMenuInDb(menuToSave, dbSuccessAction, dbErrorAction);
                    } else {
                        insertMenuToDb(menuToSave, dbSuccessAction, dbErrorAction);
                    }
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(CreateMenuActivity.this, "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
                    // Keep existing image if edit mode, or save without image
                    menuToSave.setImageUrl(isEditMode ? existingImageUrl : "");
                    if (isEditMode) {
                        updateMenuInDb(menuToSave, dbSuccessAction, dbErrorAction);
                    } else {
                        insertMenuToDb(menuToSave, dbSuccessAction, dbErrorAction);
                    }
                }
            });
        } else {
            // No new image selected
            menuToSave.setImageUrl(isEditMode ? existingImageUrl : "");
            if (isEditMode) {
                updateMenuInDb(menuToSave, dbSuccessAction, dbErrorAction);
            } else {
                insertMenuToDb(menuToSave, dbSuccessAction, dbErrorAction);
            }
        }
    }

    private void insertMenuToDb(Menu menu, Runnable onSuccess, Consumer<String> onError) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Normalize any existing bad menu_id values (long/timestamp-based) so our next ID generation is stable
                try {
                    int changed = DBMigrationHelper.normalizeMenuIds(conn);
                    if (changed > 0) {
                        Log.i(TAG, "Normalized existing menu_ids before insert: changed=" + changed);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to normalize menu ids", e);
                    // proceed anyway; generation logic below ignores long IDs
                }
                 if (conn == null) throw new SQLException("DB connection is null");

                String sql = "INSERT INTO Menu (menu_id, menu_name, image_url, description, from_date, to_date, create_at, update_at) VALUES (?,?,?,?,?,?,?,?)";
                // Determine menu id: if caller didn't provide one, generate sequential M001, M002, ...
                String id = menu.getMenuId();
                if (id == null || id.isEmpty()) {
                    // Find current max numeric suffix of menu_id like 'M123' and increment
                    // Only consider existing menu_ids in the short numeric format (M, followed by 1-3 digits)
                    try (PreparedStatement stmtMax = conn.prepareStatement(
                            "SELECT MAX(CAST(SUBSTRING(menu_id,2) AS UNSIGNED)) AS maxnum FROM Menu WHERE menu_id REGEXP '^M[0-9]{1,3}$'")) {
                        try (java.sql.ResultSet rsMax = stmtMax.executeQuery()) {
                            int max = 0;
                            if (rsMax.next()) {
                                max = rsMax.getInt("maxnum");
                                if (rsMax.wasNull()) max = 0;
                            }
                            int next = max + 1;
                            id = String.format(java.util.Locale.US, "M%03d", next);
                        }
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    // ensure id not too long (defensive)
                    if (id.length() > 16) id = id.substring(0, 16);
                    stmt.setString(1, id);
                    stmt.setString(2, menu.getMenuName());
                    stmt.setString(3, menu.getImageUrl());
                    stmt.setString(4, menu.getDescription());
                    
                    // Set from_date (optional)
                    if (menu.getFromDate() != null) {
                        stmt.setDate(5, new java.sql.Date(menu.getFromDate().getTime()));
                    } else {
                        stmt.setNull(5, java.sql.Types.DATE);
                    }
                    
                    // Set to_date (optional)
                    if (menu.getToDate() != null) {
                        stmt.setDate(6, new java.sql.Date(menu.getToDate().getTime()));
                    } else {
                        stmt.setNull(6, java.sql.Types.DATE);
                    }
                    
                    stmt.setTimestamp(7, new java.sql.Timestamp(new Date().getTime()));
                    stmt.setTimestamp(8, new java.sql.Timestamp(new Date().getTime()));

                    if (stmt.executeUpdate() <= 0) throw new SQLException("Insert failed");
                    menu.setMenuId(id);
                    runOnUiThread(onSuccess);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting menu", e);
                runOnUiThread(() -> onError.accept(e.getMessage()));
            }
        });
    }

    private void updateMenuInDb(Menu menu, Runnable onSuccess, Consumer<String> onError) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String sql = "UPDATE Menu SET menu_name = ?, image_url = ?, description = ?, from_date = ?, to_date = ?, update_at = ? WHERE menu_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, menu.getMenuName());
                    stmt.setString(2, menu.getImageUrl());
                    stmt.setString(3, menu.getDescription());
                    
                    // Set from_date (optional)
                    if (menu.getFromDate() != null) {
                        stmt.setDate(4, new java.sql.Date(menu.getFromDate().getTime()));
                    } else {
                        stmt.setNull(4, java.sql.Types.DATE);
                    }
                    
                    // Set to_date (optional)
                    if (menu.getToDate() != null) {
                        stmt.setDate(5, new java.sql.Date(menu.getToDate().getTime()));
                    } else {
                        stmt.setNull(5, java.sql.Types.DATE);
                    }
                    
                    stmt.setTimestamp(6, new java.sql.Timestamp(new Date().getTime()));
                    stmt.setString(7, menu.getMenuId());

                    if (stmt.executeUpdate() <= 0) throw new SQLException("Update failed. Menu not found.");
                    runOnUiThread(onSuccess);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating menu", e);
                runOnUiThread(() -> onError.accept(e.getMessage()));
            }
        });
    }
}
