package com.example.final_project.views.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.models.entity.Menu;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.utils.DatabaseConnection;
import com.example.final_project.utils.UserSessionManager;
import com.example.final_project.utils.CloudinaryHelper;
import com.example.final_project.utils.DBMigrationHelper;
import com.example.final_project.views.adapter.SelectedRecipeAdapter;
import com.bumptech.glide.Glide;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private static final int REQUEST_CREATE_RECIPE = 2;
    private EditText etMenuName, etMenuDescription, etFromDate, etToDate;
    private ImageView imageMenu;
    private Button btnAddRecipe;
    private Uri imageUri;
    private String existingMenuId = null;
    private String existingImageUrl = null;
    private boolean isEditMode = false;
    private static final String TAG = "CreateMenuActivity";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Track selected recipes to add to menu
    private List<Recipe> selectedRecipes = new ArrayList<>();
    private Set<String> selectedRecipeIds = new HashSet<>();

    // RecyclerView for selected recipes
    private RecyclerView recyclerSelectedRecipes;
    private SelectedRecipeAdapter selectedRecipeAdapter;

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
        btnAddRecipe = findViewById(R.id.btnAddRecipe);

        // Setup RecyclerView for selected recipes
        recyclerSelectedRecipes = findViewById(R.id.recyclerRecipeInput);
        recyclerSelectedRecipes.setLayoutManager(new LinearLayoutManager(this));
        selectedRecipeAdapter = new SelectedRecipeAdapter(selectedRecipes, this::onRemoveRecipeClicked);
        recyclerSelectedRecipes.setAdapter(selectedRecipeAdapter);

        // --- NEW: Setup IME action listeners for Enter key handling ---
        etMenuName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                etMenuDescription.requestFocus();
                return true;
            }
            return false;
        });

        etMenuDescription.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                findViewById(R.id.etMenuNote).requestFocus();
                return true;
            }
            return false;
        });

        EditText etMenuNote = findViewById(R.id.etMenuNote);
        if (etMenuNote != null) {
            etMenuNote.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Hide keyboard when Done is pressed on Note field
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null && getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
                    etMenuNote.clearFocus();
                    return true;
                }
                return false;
            });
        }

        btnUploadImage.setOnClickListener(v -> openImagePicker());
        btnSaveMenu.setOnClickListener(v -> saveMenu());
        btnBack.setOnClickListener(v -> finish());
        btnAddRecipe.setOnClickListener(v -> showAvailableRecipesDialog());

        // Add click listener to image view to allow changing image
        imageMenu.setOnClickListener(v -> openImagePicker());
        imageMenu.setOnLongClickListener(v -> {
            // Long click to clear image
            new AlertDialog.Builder(this)
                    .setTitle("Xóa ảnh")
                    .setMessage("Bạn có muốn xóa ảnh hiện tại không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        imageUri = null;
                        existingImageUrl = null;
                        imageMenu.setImageResource(R.drawable.ic_launcher_background);
                        Toast.makeText(this, "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return true;
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
                                    // Check if it's a valid URL (Cloudinary, http, https, content, file)
                                    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") ||
                                        imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                                        try {
                                            Glide.with(CreateMenuActivity.this)
                                                    .load(imageUrl)
                                                    .placeholder(R.drawable.ic_launcher_background)
                                                    .error(R.drawable.ic_launcher_background)
                                                    .into(imageMenu);
                                            Log.d(TAG, "Loading image from URL: " + imageUrl);
                                        } catch (Exception ex) {
                                            Log.e(TAG, "Failed to load image from URL: " + imageUrl, ex);
                                            imageMenu.setImageResource(R.drawable.ic_launcher_background);
                                            Toast.makeText(CreateMenuActivity.this, "Unable to load existing image", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Legacy local filename (e.g., 'diet_menu.jpg') - cannot load, show placeholder
                                        Log.w(TAG, "Legacy local filename detected: " + imageUrl + ". Cannot load. Please upload new image.");
                                        imageMenu.setImageResource(R.drawable.ic_launcher_background);
                                        Toast.makeText(CreateMenuActivity.this, "Ảnh cũ không khả dụng. Vui lòng tải ảnh mới!", Toast.LENGTH_LONG).show();
                                        // Clear the existing URL so user must upload new one
                                        existingImageUrl = null;
                                    }
                                } else {
                                    Log.d(TAG, "No image URL available");
                                    imageMenu.setImageResource(R.drawable.ic_launcher_background);
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
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                } catch (SecurityException se) {
                    Log.w(TAG, "Could not take persistable uri permission", se);
                }

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageMenu.setImageBitmap(bitmap);
                Toast.makeText(this, "Đã chọn ảnh mới", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "New image selected: " + imageUri.toString());
            } catch (IOException e) {
                Log.e(TAG, "Error loading picked image", e);
                Toast.makeText(this, "Lỗi khi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CREATE_RECIPE && resultCode == Activity.RESULT_OK) {
            // User just created a new recipe, show toast and refresh available recipes
            Toast.makeText(this, "Recipe đã được tạo! Bây giờ bạn có thể thêm vào menu.", Toast.LENGTH_LONG).show();
            // Automatically show the recipe selection dialog again
            showAvailableRecipesDialog();
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

        // Validate: Must have at least one recipe selected
        if (selectedRecipes.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất 1 recipe vào menu!", Toast.LENGTH_LONG).show();
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

        Menu menuToSave = new Menu(existingMenuId, name, null, description, fromDate, toDate, null, null, 
                UserSessionManager.getInstance(this).getRequiredUserId());

        if (imageUri != null) {
            // New image selected, upload it
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Uploading new image to Cloudinary...");
            CloudinaryHelper.uploadImage(this, imageUri, new CloudinaryHelper.UploadCallback() {
                @Override
                public void onSuccess(String newImageUrl) {
                    Log.d(TAG, "Image uploaded successfully: " + newImageUrl);
                    menuToSave.setImageUrl(newImageUrl);
                    if (isEditMode) {
                        updateMenuInDb(menuToSave, dbSuccessAction, dbErrorAction);
                    } else {
                        insertMenuToDb(menuToSave, dbSuccessAction, dbErrorAction);
                    }
                }
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Image upload failed: " + error);
                    Toast.makeText(CreateMenuActivity.this, "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
                    // Allow user to save without image or with old image
                    new AlertDialog.Builder(CreateMenuActivity.this)
                            .setTitle("Upload ảnh thất bại")
                            .setMessage("Không thể upload ảnh. Bạn có muốn:\n\n1. Lưu menu không có ảnh\n2. Giữ ảnh cũ (nếu có)\n3. Hủy và thử lại")
                            .setPositiveButton("Lưu không có ảnh", (dialog, which) -> {
                                menuToSave.setImageUrl("");
                                if (isEditMode) {
                                    updateMenuInDb(menuToSave, dbSuccessAction, dbErrorAction);
                                } else {
                                    insertMenuToDb(menuToSave, dbSuccessAction, dbErrorAction);
                                }
                            })
                            .setNeutralButton("Giữ ảnh cũ", (dialog, which) -> {
                                if (isEditMode && existingImageUrl != null && !existingImageUrl.isEmpty()) {
                                    // Only keep old image if it's a valid URL
                                    if (existingImageUrl.startsWith("http://") || existingImageUrl.startsWith("https://")) {
                                        menuToSave.setImageUrl(existingImageUrl);
                                    } else {
                                        menuToSave.setImageUrl("");
                                    }
                                } else {
                                    menuToSave.setImageUrl("");
                                }
                                if (isEditMode) {
                                    updateMenuInDb(menuToSave, dbSuccessAction, dbErrorAction);
                                } else {
                                    insertMenuToDb(menuToSave, dbSuccessAction, dbErrorAction);
                                }
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                }
            });
        } else {
            // No new image selected
            Log.d(TAG, "No new image selected");
            if (isEditMode) {
                // Keep existing image if valid, otherwise clear it
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    // Only keep if it's a valid URL
                    if (existingImageUrl.startsWith("http://") || existingImageUrl.startsWith("https://")) {
                        menuToSave.setImageUrl(existingImageUrl);
                        Log.d(TAG, "Keeping existing valid image URL: " + existingImageUrl);
                    } else {
                        // Legacy local filename - clear it
                        menuToSave.setImageUrl("");
                        Log.d(TAG, "Clearing legacy image URL: " + existingImageUrl);
                    }
                } else {
                    menuToSave.setImageUrl("");
                    Log.d(TAG, "No existing image URL");
                }
                updateMenuInDb(menuToSave, dbSuccessAction, dbErrorAction);
            } else {
                // New menu without image
                menuToSave.setImageUrl("");
                Log.d(TAG, "Creating new menu without image");
                insertMenuToDb(menuToSave, dbSuccessAction, dbErrorAction);
            }
        }
    }

    /**
     * Show dialog with available recipes (all user's recipes, allowing same recipe in multiple menus)
     */
    private void showAvailableRecipesDialog() {
        dbExecutor.execute(() -> {
            List<Recipe> availableRecipes = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String currentUserId = UserSessionManager.getInstance(this).getCurrentUserId();

                // Get ALL recipes created by the user
                // A recipe can be in multiple menus, so we don't filter by RecipeInMenu
                // We only exclude recipes already selected in the current editing session
                String sql = "SELECT r.recipe_id, r.name, r.image_url, r.instruction, r.nutrition " +
                            "FROM Recipe r " +
                            "WHERE r.user_id = ? " +
                            "ORDER BY r.create_at DESC";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, currentUserId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Recipe recipe = new Recipe();
                            recipe.setRecipeId(rs.getString("recipe_id"));
                            recipe.setName(rs.getString("name"));
                            recipe.setImageUrl(rs.getString("image_url"));
                            recipe.setInstruction(rs.getString("instruction"));
                            recipe.setNutrition(rs.getString("nutrition"));
                            availableRecipes.add(recipe);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading available recipes", e);
                runOnUiThread(() -> Toast.makeText(this, "Error loading recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                return;
            }

            runOnUiThread(() -> {
                if (availableRecipes.isEmpty()) {
                    // No recipes available, show dialog to create new recipe
                    new AlertDialog.Builder(this)
                            .setTitle("Không có Recipe")
                            .setMessage("Bạn chưa có recipe nào. Bạn có muốn tạo recipe mới không?")
                            .setPositiveButton("Tạo Recipe", (dialog, which) -> {
                                // Go to Create Recipe activity
                                Intent intent = new Intent(this, CreateRecipeActivity.class);
                                intent.putExtra("from_create_menu", true); // Flag to know we came from Create Menu
                                startActivityForResult(intent, REQUEST_CREATE_RECIPE);
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                    return;
                }

                showRecipeSelectionDialog(availableRecipes);
            });
        });
    }

    /**
     * Show dialog to select multiple recipes
     */
    private void showRecipeSelectionDialog(List<Recipe> recipes) {
        String[] recipeNames = new String[recipes.size()];
        boolean[] checkedItems = new boolean[recipes.size()];

        for (int i = 0; i < recipes.size(); i++) {
            recipeNames[i] = recipes.get(i).getName();
            // Check if already selected
            checkedItems[i] = selectedRecipeIds.contains(recipes.get(i).getRecipeId());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Recipe để thêm vào Menu")
                .setMultiChoiceItems(recipeNames, checkedItems, (dialog, which, isChecked) -> {
                    Recipe recipe = recipes.get(which);
                    if (isChecked) {
                        if (!selectedRecipeIds.contains(recipe.getRecipeId())) {
                            selectedRecipes.add(recipe);
                            selectedRecipeIds.add(recipe.getRecipeId());
                        }
                    } else {
                        selectedRecipes.removeIf(r -> r.getRecipeId().equals(recipe.getRecipeId()));
                        selectedRecipeIds.remove(recipe.getRecipeId());
                    }
                })
                .setPositiveButton("Xong", (dialog, which) -> {
                    if (selectedRecipes.isEmpty()) {
                        Toast.makeText(this, "Chưa chọn recipe nào", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Đã chọn " + selectedRecipes.size() + " recipe(s)", Toast.LENGTH_SHORT).show();
                        // Update RecyclerView to show selected recipes
                        selectedRecipeAdapter.notifyDataSetChanged();
                    }
                })
                .setNeutralButton("Tạo Recipe Mới", (dialog, which) -> {
                    // User wants to create a new recipe while choosing
                    Intent intent = new Intent(this, CreateRecipeActivity.class);
                    intent.putExtra("from_create_menu", true);
                    startActivityForResult(intent, REQUEST_CREATE_RECIPE);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Handle remove recipe button click with confirmation dialog
     */
    private void onRemoveRecipeClicked(Recipe recipe, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa Recipe")
                .setMessage("Bạn có chắc chắn muốn xóa \"" + recipe.getName() + "\" khỏi menu không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Remove from lists
                    selectedRecipes.remove(position);
                    selectedRecipeIds.remove(recipe.getRecipeId());

                    // Update RecyclerView
                    selectedRecipeAdapter.notifyItemRemoved(position);
                    selectedRecipeAdapter.notifyItemRangeChanged(position, selectedRecipes.size());

                    Toast.makeText(this, "Đã xóa \"" + recipe.getName() + "\"", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Removed recipe: " + recipe.getName() + " from selection");
                })
                .setNegativeButton("Hủy", null)
                .show();
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

                String sql = "INSERT INTO Menu (menu_id, menu_name, image_url, description, from_date, to_date, create_at, update_at, user_id) VALUES (?,?,?,?,?,?,?,?,?)";
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
                    stmt.setString(9, menu.getUserId());

                    if (stmt.executeUpdate() <= 0) throw new SQLException("Insert failed");
                    menu.setMenuId(id);

                    // After inserting menu, insert selected recipes into RecipeInMenu
                    if (!selectedRecipes.isEmpty()) {
                        insertRecipesIntoMenu(conn, id, selectedRecipes);
                    }

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

                String sql = "UPDATE Menu SET menu_name = ?, image_url = ?, description = ?, from_date = ?, to_date = ?, update_at = ? WHERE menu_id = ? AND user_id = ?";
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
                    stmt.setString(8, menu.getUserId());

                    if (stmt.executeUpdate() <= 0) throw new SQLException("Update failed. Menu not found.");
                    runOnUiThread(onSuccess);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating menu", e);
                runOnUiThread(() -> onError.accept(e.getMessage()));
            }
        });
    }

    /**
     * Insert selected recipes into RecipeInMenu table
     */
    private void insertRecipesIntoMenu(Connection conn, String menuId, List<Recipe> recipes) throws SQLException {
        String sql = "INSERT INTO RecipeInMenu (recipeMenu_id, menu_id, recipe_id) VALUES (?, ?, ?)";

        for (Recipe recipe : recipes) {
            // Generate recipeMenu_id
            String recipeMenuId = generateRecipeMenuId(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, recipeMenuId);
                stmt.setString(2, menuId);
                stmt.setString(3, recipe.getRecipeId());
                stmt.executeUpdate();
                Log.d(TAG, "Inserted recipe " + recipe.getName() + " into menu " + menuId);
            }
        }
    }

    /**
     * Generate unique recipeMenu_id
     */
    private String generateRecipeMenuId(Connection conn) throws SQLException {
        // Find max numeric suffix and increment
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT MAX(CAST(SUBSTRING(recipeMenu_id, 3) AS UNSIGNED)) AS maxnum FROM RecipeInMenu WHERE recipeMenu_id REGEXP '^RM[0-9]+$'")) {
            try (ResultSet rs = stmt.executeQuery()) {
                int max = 0;
                if (rs.next()) {
                    max = rs.getInt("maxnum");
                    if (rs.wasNull()) max = 0;
                }
                int next = max + 1;
                return String.format(Locale.US, "RM%03d", next);
            }
        }
    }
}
