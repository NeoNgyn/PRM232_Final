package com.example.final_project.views.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_project.R;
import com.example.final_project.BuildConfig;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.utils.DatabaseConnection;
import com.example.final_project.utils.UserSessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CreateRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etRecipeName, etRecipeInstruction, etRecipeNutrition;
    private ImageView imageRecipePreview;
    private Button btnChooseImage, btnSaveRecipe;
    private Uri imageUri;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    // executor dedicated for uploads
    private final ExecutorService uploadExecutor = Executors.newSingleThreadExecutor();
    private String menuId;
    private Recipe editingRecipe = null;
    private ProgressDialog progressDialog;

    // --- New fields for ingredient handling ---
    private Spinner spFoodItem;
    private Spinner spUnit;
    private EditText etIngredientAmount;
    private Button btnAddIngredient;
    private LinearLayout llIngredientsContainer;

    // Local caches
    private final List<String> foodItemIds = new ArrayList<>();
    private final List<String> foodItemNames = new ArrayList<>();
    private final List<Integer> foodItemUnitIds = new ArrayList<>(); // Store unit_id for each food item
    private final List<String> foodItemUnitNames = new ArrayList<>(); // Store unit_name for each food item
    private final List<Integer> unitIds = new ArrayList<>();
    private final List<String> unitNames = new ArrayList<>();

    // In-memory ingredient representation
    private static class IngredientEntry {
        String foodId;
        String foodName;
        String amount; // keep as string for easy DB insert (can hold decimals)
        Integer unitId; // may be null
        String unitName;

        IngredientEntry(String foodId, String foodName, String amount, Integer unitId, String unitName) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.amount = amount;
            this.unitId = unitId;
            this.unitName = unitName;
        }
    }

    private final List<IngredientEntry> ingredientsList = new ArrayList<>();

    // Lưu padding-top gốc của header để không cộng dồn khi onResume
    private int headerOriginalPaddingTop = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        // Cố định header: lấy view headerContainer (đã thêm trong layout) và đảm bảo nó luôn ở trên cùng
        View header = findViewById(R.id.headerContainer);
        if (header != null) {
            if (headerOriginalPaddingTop == -1) {
                headerOriginalPaddingTop = header.getPaddingTop();
            }
            int statusBarHeight = getStatusBarHeight();
            header.setPadding(header.getPaddingLeft(), headerOriginalPaddingTop + statusBarHeight, header.getPaddingRight(), header.getPaddingBottom());
            header.bringToFront();
            header.requestLayout();
            header.invalidate();
        }

        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        etRecipeName = findViewById(R.id.etRecipeName);
        etRecipeInstruction = findViewById(R.id.etRecipeInstruction);
        etRecipeNutrition = findViewById(R.id.etRecipeNutrition);
        imageRecipePreview = findViewById(R.id.imageRecipePreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSaveRecipe = findViewById(R.id.btnSaveRecipe);

        // Ingredient UI refs
        spFoodItem = findViewById(R.id.spFoodItem);
        spUnit = findViewById(R.id.spUnit);
        etIngredientAmount = findViewById(R.id.etIngredientAmount);
        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        llIngredientsContainer = findViewById(R.id.llIngredientsContainer);

        // Đổi header thành Edit nếu là chỉnh sửa
        android.widget.TextView tvHeader = findViewById(R.id.tvHeader);
        Intent intent = getIntent();
        editingRecipe = (Recipe) intent.getSerializableExtra("recipe");
        menuId = intent.getStringExtra("menu_id");
        if (editingRecipe != null) {
            if (tvHeader != null) tvHeader.setText(R.string.edit_recipe_header);
            etRecipeName.setText(editingRecipe.getName() != null ? editingRecipe.getName() : "");
            etRecipeInstruction.setText(editingRecipe.getInstruction() != null ? editingRecipe.getInstruction() : "");
            etRecipeNutrition.setText(editingRecipe.getNutrition() != null ? editingRecipe.getNutrition() : "");
            String imageUrl = editingRecipe.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                imageUri = Uri.parse(imageUrl);
                com.bumptech.glide.Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_food_placeholder)
                        .error(R.drawable.ic_food_placeholder)
                        .into(imageRecipePreview);
            } else {
                imageRecipePreview.setImageResource(R.drawable.ic_food_placeholder);
            }

            // If editing, load existing ingredients for this recipe into in-memory list then show
            loadIngredientsForEditing(editingRecipe.getRecipeId());
        } else {
            if (tvHeader != null) tvHeader.setText(R.string.create_recipe_header);
        }

        btnChooseImage.setOnClickListener(v -> openImagePicker());
        btnSaveRecipe.setOnClickListener(v -> saveRecipe());

        // Allow clicking on the image to change it
        imageRecipePreview.setOnClickListener(v -> openImagePicker());

        // Load units and food items from DB
        loadUnitsAndFoodItems();

        btnAddIngredient.setOnClickListener(v -> onAddIngredientClicked());

        // --- NEW: handle IME actions for Enter key navigation and actions ---
        etRecipeName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                etRecipeInstruction.requestFocus();
                return true;
            }
            return false;
        });

        etRecipeInstruction.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                etRecipeNutrition.requestFocus();
                return true;
            }
            return false;
        });

        etRecipeNutrition.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                // hide keyboard and move focus to ingredient amount
                hideKeyboard();
                etIngredientAmount.requestFocus();
                return true;
            }
            return false;
        });

        etIngredientAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                hideKeyboard();
                // Just hide keyboard and keep the number visible in the field (don't add to list yet)
                // User will press Add button when ready to add ingredient
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure header remains on top after resume
        View header = findViewById(R.id.headerContainer);
        if (header != null) {
            if (headerOriginalPaddingTop == -1) {
                headerOriginalPaddingTop = header.getPaddingTop();
            }
            int statusBarHeight = getStatusBarHeight();
            header.setPadding(header.getPaddingLeft(), headerOriginalPaddingTop + statusBarHeight, header.getPaddingRight(), header.getPaddingBottom());
            header.bringToFront();
            header.requestLayout();
            header.invalidate();
        }
    }

    // Trợ giúp lấy chiều cao status bar
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // Helper to hide keyboard
    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View current = getCurrentFocus();
            if (imm != null && current != null) {
                imm.hideSoftInputFromWindow(current.getWindowToken(), 0);
            }
        } catch (Exception e) {
            android.util.Log.w("CreateRecipeActivity", "hideKeyboard failed", e);
        }
    }

    // Load existing ingredient rows when editing a recipe (if editingRecipe != null)
    private void loadIngredientsForEditing(String recipeId) {
        if (recipeId == null) return;
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) return;
                // Fixed: Get unit_id from FoodItem table, not from Ingredient table
                String sql = "SELECT i.amount, i.food_id, fi.food_name, fi.unit_id, u.unit_name " +
                        "FROM Ingredient i " +
                        "LEFT JOIN FoodItem fi ON i.food_id = fi.food_id " +
                        "LEFT JOIN Unit u ON fi.unit_id = u.unit_id " +
                        "WHERE i.recipe_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, recipeId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String amount = rs.getString("amount");
                            String foodId = rs.getString("food_id");
                            String foodName = rs.getString("food_name");
                            int unitId = rs.getInt("unit_id");
                            String unitName = rs.getString("unit_name");
                            IngredientEntry e = new IngredientEntry(foodId, foodName != null ? foodName : foodId, amount, unitId == 0 ? null : unitId, unitName);
                            ingredientsList.add(e);
                        }
                    }
                }
                runOnUiThread(this::refreshIngredientsUI);
            } catch (Exception e) {
                android.util.Log.e("CreateRecipeActivity", "Error loading ingredients for edit", e);
            }
        });
    }

    private void onAddIngredientClicked() {
        // Original behavior: add current field value and refresh
        int foodPos = spFoodItem.getSelectedItemPosition();
        String amount = etIngredientAmount.getText().toString().trim();
        if (foodPos < 0 || foodPos >= foodItemIds.size()) {
            Toast.makeText(this, "Please select a food item", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amount.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String foodId = foodItemIds.get(foodPos);
        String foodName = foodItemNames.get(foodPos);
        Integer unitId = foodPos < foodItemUnitIds.size() ? foodItemUnitIds.get(foodPos) : null;
        String unitName = foodPos < foodItemUnitNames.size() ? foodItemUnitNames.get(foodPos) : "";

        IngredientEntry entry = new IngredientEntry(foodId, foodName, amount, unitId, unitName);
        ingredientsList.add(entry);
        etIngredientAmount.setText("");
        refreshIngredientsUI();
        android.util.Log.d("CreateRecipeActivity", "Added ingredient: " + amount + " " + unitName + " " + foodName);
    }

    private void refreshIngredientsUI() {
        llIngredientsContainer.removeAllViews();
        for (int i = 0; i < ingredientsList.size(); i++) {
            final int idx = i;
            IngredientEntry it = ingredientsList.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(6,6,6,6);

            TextView tv = new TextView(this);
            String text = it.amount + (it.unitName != null && !it.unitName.isEmpty() ? " " + it.unitName : "") + " " + it.foodName;
            tv.setText(text);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            Button btnRemove = new Button(this);
            btnRemove.setText("Remove");
            btnRemove.setOnClickListener(v -> {
                ingredientsList.remove(idx);
                refreshIngredientsUI();
            });

            row.addView(tv);
            row.addView(btnRemove);
            llIngredientsContainer.addView(row);
        }
    }

    private void loadUnitsAndFoodItems() {
        // Load in background then populate spinners on UI thread
        dbExecutor.execute(() -> {
            // Get current logged-in user ID from session
            String userId = UserSessionManager.getInstance(this).getCurrentUserId();
            if (userId == null || userId.isEmpty()) {
                android.util.Log.e("CreateRecipeActivity", "User not logged in");
                runOnUiThread(() -> Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show());
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) return;

                // Load units
                String sqlUnits = "SELECT unit_id, unit_name FROM Unit ORDER BY unit_id";
                try (PreparedStatement stmt = conn.prepareStatement(sqlUnits); ResultSet rs = stmt.executeQuery()) {
                    unitIds.clear();
                    unitNames.clear();
                    while (rs.next()) {
                        unitIds.add(rs.getInt("unit_id"));
                        unitNames.add(rs.getString("unit_name"));
                    }
                }

                // Load food items WITH their unit_id and unit_name for the user
                String sqlFoods = "SELECT fi.food_id, fi.food_name, fi.unit_id, u.unit_name " +
                        "FROM FoodItem fi " +
                        "LEFT JOIN Unit u ON fi.unit_id = u.unit_id " +
                        "WHERE fi.user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlFoods)) {
                    stmt.setString(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        foodItemIds.clear();
                        foodItemNames.clear();
                        foodItemUnitIds.clear();
                        foodItemUnitNames.clear();
                        while (rs.next()) {
                            foodItemIds.add(rs.getString("food_id"));
                            foodItemNames.add(rs.getString("food_name"));
                            int unitId = rs.getInt("unit_id");
                            String unitName = rs.getString("unit_name");
                            foodItemUnitIds.add(unitId == 0 ? null : unitId);
                            foodItemUnitNames.add(unitName != null ? unitName : "");
                        }
                    }
                }

                runOnUiThread(() -> {
                    // Populate unit spinner (for reference only)
                    ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(CreateRecipeActivity.this, android.R.layout.simple_spinner_item, unitNames);
                    unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spUnit.setAdapter(unitAdapter);

                    // Populate food item spinner with "food_name (unit_name)" format for clarity
                    List<String> foodDisplayNames = new ArrayList<>();
                    for (int i = 0; i < foodItemNames.size(); i++) {
                        String foodName = foodItemNames.get(i);
                        String unitName = i < foodItemUnitNames.size() ? foodItemUnitNames.get(i) : "";
                        if (unitName != null && !unitName.isEmpty()) {
                            foodDisplayNames.add(foodName + " (" + unitName + ")");
                        } else {
                            foodDisplayNames.add(foodName);
                        }
                    }
                    ArrayAdapter<String> foodAdapter = new ArrayAdapter<>(CreateRecipeActivity.this, android.R.layout.simple_spinner_item, foodDisplayNames);
                    foodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spFoodItem.setAdapter(foodAdapter);

                    // Hide or disable the unit spinner since unit is determined by food item
                    spUnit.setEnabled(false);
                    spUnit.setVisibility(android.view.View.GONE);

                    // If editing and there are existing ingredients, keep them shown (they were loaded earlier)
                    refreshIngredientsUI();
                });

            } catch (Exception e) {
                android.util.Log.e("CreateRecipeActivity", "Error loading units/food items", e);
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        // Use ACTION_OPEN_DOCUMENT so we can request persistable permission for the chosen Uri
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        // When wrapping with a chooser, make sure the chooser itself carries the permission flags
        Intent chooser = Intent.createChooser(intent, "Select Recipe Image");
        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(chooser, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            android.util.Log.d("CreateRecipeActivity", "Image selected: " + imageUri.toString());

            try {
                // Try to persist URI permission so later Activities (e.g., RecipeListActivity) can read it
                try {
                    int dataFlags = data.getFlags();
                    // Call persistable permission for read and write separately to satisfy lint annotations
                    if ((dataFlags & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
                        try {
                            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException inner) {
                            android.util.Log.w("CreateRecipeActivity", "Could not take persistable read uri permission", inner);
                        }
                    }
                    if ((dataFlags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0) {
                        try {
                            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } catch (SecurityException inner) {
                            android.util.Log.w("CreateRecipeActivity", "Could not take persistable write uri permission", inner);
                        }
                    }
                    // If chooser didn't propagate flags at all, best-effort request read permission
                    if ((dataFlags & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)) == 0) {
                        try {
                            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException inner) {
                            android.util.Log.w("CreateRecipeActivity", "Could not take persistable uri permission (fallback)", inner);
                        }
                    }
                } catch (SecurityException se) {
                    android.util.Log.w("CreateRecipeActivity", "Could not take persistable uri permission", se);
                }

                // Load and display the selected image immediately
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                if (bitmap != null) {
                    imageRecipePreview.setImageBitmap(bitmap);
                    // Force refresh the ImageView
                    imageRecipePreview.invalidate();
                    imageRecipePreview.requestLayout();

                    // Show success feedback
                    Toast.makeText(this, "✅ Đã chọn ảnh mới", Toast.LENGTH_SHORT).show();
                    android.util.Log.d("CreateRecipeActivity", "Image loaded and displayed successfully");
                } else {
                    Toast.makeText(this, "⚠️ Không thể tải ảnh", Toast.LENGTH_SHORT).show();
                    android.util.Log.e("CreateRecipeActivity", "Bitmap is null after loading");
                }
            } catch (IOException e) {
                android.util.Log.e("CreateRecipeActivity", "Error loading picked image", e);
                Toast.makeText(this, "❌ Lỗi khi tải ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
                // Show placeholder on error
                imageRecipePreview.setImageResource(R.drawable.ic_food_placeholder);
            }
        } else {
            android.util.Log.w("CreateRecipeActivity", "Image selection cancelled or invalid");
        }
    }

    // Helper: copy a content Uri to a temporary file in cache and return the File
    private File uriToTempFile(Uri uri) {
        if (uri == null) return null;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            File temp = File.createTempFile("upload_", ".jpg", getCacheDir());
            try (OutputStream out = new FileOutputStream(temp)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            return temp;
        } catch (Exception e) {
            android.util.Log.e("CreateRecipeActivity", "Failed to copy uri to temp file", e);
            return null;
        }
    }

    // Upload file to Cloudinary using unsigned upload preset configured in build config.
    // Returns secure_url (https) on success or null on failure.
    private String uploadFileToCloudinary(File file) {
        if (file == null || !file.exists()) return null;
        final String cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME != null ? BuildConfig.CLOUDINARY_CLOUD_NAME : "";
        final String uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET != null ? BuildConfig.CLOUDINARY_UPLOAD_PRESET : "";
        if (cloudName.isEmpty() || uploadPreset.isEmpty()) {
            android.util.Log.e("CreateRecipeActivity", "Cloudinary configuration missing in BuildConfig");
            return null;
        }
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("image/*");
        RequestBody fileBody = RequestBody.create(file, mediaType);
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .addFormDataPart("upload_preset", uploadPreset)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                android.util.Log.e("CreateRecipeActivity", "Cloudinary upload failed: " + response.code() + " " + response.message());
                return null;
            }
            String body = response.body() != null ? response.body().string() : null;
            if (body == null) return null;
            JSONObject json = new JSONObject(body);
            return json.optString("secure_url", null);
        } catch (Exception e) {
            android.util.Log.e("CreateRecipeActivity", "Exception uploading to Cloudinary", e);
            return null;
        }
    }

    private void saveRecipe() {
        String name = etRecipeName.getText().toString().trim();
        String instruction = etRecipeInstruction.getText().toString().trim();
        String nutrition = etRecipeNutrition.getText().toString().trim();
        if (name.isEmpty() || instruction.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // (no pending buffer used) -- proceed to save directly

        // Note: menuId can be null - this means creating a standalone recipe (not attached to any menu yet)
        android.util.Log.d("CreateRecipeActivity", "Saving recipe. MenuId: " + (menuId != null ? menuId : "null (standalone recipe)"));

        // Determine whether we need to upload the selected image to Cloudinary.
        final String currentImageString = (imageUri != null) ? imageUri.toString() : (editingRecipe != null ? editingRecipe.getImageUrl() : null);
        boolean needsUpload = false;
        if (imageUri != null) {
            String s = imageUri.toString();
            if (!(s.startsWith("http://") || s.startsWith("https://"))) {
                // Local content/file Uris need upload
                needsUpload = true;
            }
        }

        if (needsUpload) {
            // Show progress dialog
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang upload ảnh...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Toast.makeText(this, "Đang upload ảnh...", Toast.LENGTH_SHORT).show();
            final Uri toUpload = imageUri;
            uploadExecutor.execute(() -> {
                File temp = uriToTempFile(toUpload);
                String uploadedUrl = uploadFileToCloudinary(temp);
                if (temp != null && temp.exists()) {
                    // Best-effort delete temp file
                    try { temp.delete(); } catch (Exception ignore) {}
                }

                // Update progress
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.setMessage("Đang lưu recipe...");
                    }
                });

                if (uploadedUrl == null) {
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(CreateRecipeActivity.this, "❌ Upload ảnh thất bại. Recipe không được lưu.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                // proceed to save with uploadedUrl
                performDatabaseSave(name, instruction, nutrition, uploadedUrl);
            });
        } else {
            // No upload required - show progress for DB save
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang lưu recipe...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // No upload required (either no image selected or image is already a web URL)
            performDatabaseSave(name, instruction, nutrition, currentImageString);
        }
    }

    // Perform DB insert/update on dbExecutor. imageUrl may be null or empty string.
    private void performDatabaseSave(String name, String instruction, String nutrition, String imageUrl) {
        dbExecutor.execute(() -> {
            try (Connection conn = com.example.final_project.utils.DatabaseConnection.getConnection()) {
                if (conn == null) {
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(this, "❌ Không thể kết nối database!", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                if (editingRecipe != null && editingRecipe.getRecipeId() != null) {
                    // Get current logged-in user ID
                    String currentUserId = UserSessionManager.getInstance(this).getCurrentUserId();
                    if (currentUserId == null || currentUserId.isEmpty()) {
                        runOnUiThread(() -> {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(this, "❌ User not logged in!", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    // UPDATE Recipe with user_id and update_at
                    String sqlUpdate = "UPDATE Recipe SET name=?, instruction=?, nutrition=?, image_url=?, user_id=?, update_at=NOW() WHERE recipe_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                        stmt.setString(1, name);
                        stmt.setString(2, instruction);
                        stmt.setString(3, nutrition);
                        stmt.setString(4, imageUrl != null ? imageUrl : "");
                        stmt.setString(5, currentUserId);
                        stmt.setString(6, editingRecipe.getRecipeId());
                        int rowsAffected = stmt.executeUpdate();
                        android.util.Log.d("CreateRecipeActivity", "Updated recipe " + editingRecipe.getRecipeId() + ", rows affected: " + rowsAffected);
                    }

                    // Replace existing ingredients for this recipe: delete then insert current list
                    try (PreparedStatement del = conn.prepareStatement("DELETE FROM Ingredient WHERE recipe_id = ?")) {
                        del.setString(1, editingRecipe.getRecipeId());
                        del.executeUpdate();
                    }
                    insertIngredientsForRecipe(conn, editingRecipe.getRecipeId());

                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(this, "✅ Recipe đã được cập nhật!", Toast.LENGTH_SHORT).show();
                        Intent res = new Intent();
                        res.putExtra("ingredients_updated", true);
                        setResult(Activity.RESULT_OK, res);
                        finish();
                    });
                } else {
                    // ...existing code for INSERT...
                    // Get current logged-in user ID
                    String currentUserId = UserSessionManager.getInstance(this).getCurrentUserId();
                    if (currentUserId == null || currentUserId.isEmpty()) {
                        runOnUiThread(() -> {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(this, "❌ User not logged in!", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    // Generate sequential recipe_id like R001, R002, ... by checking existing IDs in DB
                    String recipeId;
                    try (PreparedStatement stmtMax = conn.prepareStatement(
                            "SELECT MAX(CAST(SUBSTRING(recipe_id,2) AS UNSIGNED)) AS maxnum FROM Recipe WHERE recipe_id REGEXP '^R[0-9]+'") ) {
                        try (java.sql.ResultSet rsMax = stmtMax.executeQuery()) {
                            int max = 0;
                            if (rsMax.next()) {
                                max = rsMax.getInt("maxnum");
                                if (rsMax.wasNull()) max = 0;
                            }
                            int next = max + 1;
                            recipeId = String.format(Locale.US, "R%03d", next);
                        }
                    }

                    // Insert vào Recipe với user_id
                    String sqlRecipe = "INSERT INTO Recipe (recipe_id, name, instruction, nutrition, image_url, user_id, create_at, update_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlRecipe)) {
                        stmt.setString(1, recipeId);
                        stmt.setString(2, name);
                        stmt.setString(3, instruction);
                        stmt.setString(4, nutrition);
                        stmt.setString(5, imageUrl != null ? imageUrl : "");
                        stmt.setString(6, currentUserId);
                        stmt.executeUpdate();
                        android.util.Log.d("CreateRecipeActivity", "Inserted recipe: " + recipeId + " for user: " + currentUserId);
                    }

                    // Only insert into RecipeInMenu if menuId is provided
                    if (menuId != null && !menuId.isEmpty()) {
                        // ...existing code for RecipeInMenu...
                        // Generate sequential recipeMenu_id like RM001, RM002, ...
                        String recipeMenuId;
                        try (PreparedStatement stmtMaxRM = conn.prepareStatement(
                                "SELECT MAX(CAST(SUBSTRING(recipeMenu_id,3) AS UNSIGNED)) AS maxnum FROM RecipeInMenu WHERE recipeMenu_id REGEXP '^RM[0-9]+'")) {
                            try (java.sql.ResultSet rsMaxRM = stmtMaxRM.executeQuery()) {
                                int maxrm = 0;
                                if (rsMaxRM.next()) {
                                    maxrm = rsMaxRM.getInt("maxnum");
                                    if (rsMaxRM.wasNull()) maxrm = 0;
                                }
                                int nextrm = maxrm + 1;
                                // Use 2-digit format (RM01, RM02, ...) to match existing original data
                                recipeMenuId = String.format(Locale.US, "RM%02d", nextrm);
                            }
                        }

                        // Insert vào RecipeInMenu
                        String sqlRecipeInMenu = "INSERT INTO RecipeInMenu (recipeMenu_id, recipe_id, menu_id) VALUES (?, ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(sqlRecipeInMenu)) {
                            stmt.setString(1, recipeMenuId);
                            stmt.setString(2, recipeId);
                            stmt.setString(3, menuId);
                            stmt.executeUpdate();
                            android.util.Log.d("CreateRecipeActivity", "Linked recipe to menu: " + recipeMenuId);
                        }
                    } else {
                        android.util.Log.d("CreateRecipeActivity", "No menuId provided - created standalone recipe");
                    }

                    // Insert ingredients for the new recipe
                    insertIngredientsForRecipe(conn, recipeId);

                    final String successMessage = (menuId != null && !menuId.isEmpty())
                        ? "✅ Recipe '" + name + "' đã được tạo và thêm vào menu!"
                        : "✅ Recipe '" + name + "' đã được tạo thành công!";

                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show();
                        Intent res = new Intent();
                        res.putExtra("ingredients_updated", true);
                        setResult(Activity.RESULT_OK, res);
                        finish();
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("CreateRecipeActivity", "Error saving recipe", e);
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(this, "❌ Lỗi khi lưu recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Insert ingredient rows for a given recipe_id using the current in-memory ingredientsList.
     * According to the database schema, Ingredient table has: ingredient_id, amount, recipe_id, food_id
     * (unit_id is stored in FoodItem table, not in Ingredient table)
     */
    private void insertIngredientsForRecipe(Connection conn, String recipeId) {
        if (ingredientsList.isEmpty()) {
            android.util.Log.w("CreateRecipeActivity", "ingredientsList is empty, no ingredients to insert");
            return;
        }
        android.util.Log.d("CreateRecipeActivity", "Inserting " + ingredientsList.size() + " ingredients for recipe: " + recipeId);
        try {
            // Find current max numeric suffix for ingredient_id
            String nextIdPrefix = "I"; // I001 etc
            int nextNum = 1;
            try (PreparedStatement stmtMax = conn.prepareStatement("SELECT MAX(CAST(SUBSTRING(ingredient_id,2) AS UNSIGNED)) AS maxnum FROM Ingredient WHERE ingredient_id REGEXP '^I[0-9]+'")) {
                try (ResultSet rs = stmtMax.executeQuery()) {
                    int max = 0;
                    if (rs.next()) {
                        max = rs.getInt("maxnum");
                        if (rs.wasNull()) max = 0;
                    }
                    nextNum = max + 1;
                }
            }

            // Fixed: Ingredient table only has ingredient_id, amount, recipe_id, food_id (no unit_id)
            String sqlInsert = "INSERT INTO Ingredient (ingredient_id, amount, recipe_id, food_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ins = conn.prepareStatement(sqlInsert)) {
                for (IngredientEntry ie : ingredientsList) {
                    String ingredientId = String.format(Locale.US, "%s%03d", nextIdPrefix, nextNum);
                    ins.setString(1, ingredientId);
                    ins.setString(2, ie.amount != null ? ie.amount : "");
                    ins.setString(3, recipeId);
                    ins.setString(4, ie.foodId);
                    ins.executeUpdate();
                    android.util.Log.d("CreateRecipeActivity", "Inserted ingredient: " + ingredientId + " - " + ie.amount + " " + ie.foodName);
                    nextNum++;
                }
            }
            android.util.Log.d("CreateRecipeActivity", "Successfully inserted all ingredients");
        } catch (Exception e) {
            android.util.Log.e("CreateRecipeActivity", "Error inserting ingredients", e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
