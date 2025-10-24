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
import com.example.final_project.models.entity.Recipe;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etRecipeName, etRecipeInstruction, etRecipeNutrition;
    private ImageView imageRecipePreview;
    private Button btnChooseImage, btnSaveRecipe;
    private Uri imageUri;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private String menuId;
    private Recipe editingRecipe = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);
        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        etRecipeName = findViewById(R.id.etRecipeName);
        etRecipeInstruction = findViewById(R.id.etRecipeInstruction);
        etRecipeNutrition = findViewById(R.id.etRecipeNutrition);
        imageRecipePreview = findViewById(R.id.imageRecipePreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSaveRecipe = findViewById(R.id.btnSaveRecipe);

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
        } else {
            if (tvHeader != null) tvHeader.setText(R.string.create_recipe_header);
        }
        btnChooseImage.setOnClickListener(v -> openImagePicker());
        btnSaveRecipe.setOnClickListener(v -> saveRecipe());
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

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageRecipePreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                android.util.Log.e("CreateRecipeActivity", "Error loading picked image", e);
            }
        }
    }

    private void saveRecipe() {
        String name = etRecipeName.getText().toString().trim();
        String instruction = etRecipeInstruction.getText().toString().trim();
        String nutrition = etRecipeNutrition.getText().toString().trim();
        String imageUrl = (imageUri != null) ? imageUri.toString() : (editingRecipe != null ? editingRecipe.getImageUrl() : null);
        if (name.isEmpty() || instruction.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chỉ kiểm tra menuId khi tạo mới recipe, không cần khi edit
        if (editingRecipe == null && (menuId == null || menuId.isEmpty())) {
            Toast.makeText(this, "Menu ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        dbExecutor.execute(() -> {
            try (Connection conn = com.example.final_project.utils.DatabaseConnection.getConnection()) {
                if (conn == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Database connection failed!", Toast.LENGTH_SHORT).show());
                    return;
                }
                if (editingRecipe != null && editingRecipe.getRecipeId() != null) {
                    // UPDATE Recipe
                    String sqlUpdate = "UPDATE Recipe SET name=?, instruction=?, nutrition=?, image_url=? WHERE recipe_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                        stmt.setString(1, name);
                        stmt.setString(2, instruction);
                        stmt.setString(3, nutrition);
                        stmt.setString(4, imageUrl != null ? imageUrl : "");
                        stmt.setString(5, editingRecipe.getRecipeId());
                        int rowsAffected = stmt.executeUpdate();
                        android.util.Log.d("CreateRecipeActivity", "Updated recipe " + editingRecipe.getRecipeId() + ", rows affected: " + rowsAffected);
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Recipe updated!", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
                } else {
                    // Generate sequential recipe_id like R001, R002, ... by checking existing IDs in DB
                    String recipeId;
                    String recipeMenuId;
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
                    // Generate sequential recipeMenu_id like RM001, RM002, ...
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
                    // Insert vào Recipe
                    String sqlRecipe = "INSERT INTO Recipe (recipe_id, name, instruction, nutrition, image_url) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlRecipe)) {
                        stmt.setString(1, recipeId);
                        stmt.setString(2, name);
                        stmt.setString(3, instruction);
                        stmt.setString(4, nutrition);
                        stmt.setString(5, imageUrl != null ? imageUrl : "");
                        stmt.executeUpdate();
                    }
                    // Insert vào RecipeInMenu
                    String sqlRecipeInMenu = "INSERT INTO RecipeInMenu (recipeMenu_id, recipe_id, menu_id) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlRecipeInMenu)) {
                        stmt.setString(1, recipeMenuId);
                        stmt.setString(2, recipeId);
                        stmt.setString(3, menuId);
                        stmt.executeUpdate();
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Recipe '" + name + "' created and added to menu!", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("CreateRecipeActivity", "Error saving recipe", e);
                runOnUiThread(() -> Toast.makeText(this, "Error saving recipe!", Toast.LENGTH_SHORT).show());
            }
        });
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
