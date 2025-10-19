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
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etRecipeName, etRecipeDescription, etRecipeNote;
    private ImageView imageRecipePreview;
    private Button btnChooseImage, btnSaveRecipe;
    private Uri imageUri;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private String menuId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        etRecipeName = findViewById(R.id.etRecipeName);
        etRecipeDescription = findViewById(R.id.etRecipeDescription);
        etRecipeNote = findViewById(R.id.etRecipeNote);
        imageRecipePreview = findViewById(R.id.imageRecipePreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSaveRecipe = findViewById(R.id.btnSaveRecipe);

        // Nếu có dữ liệu truyền sang thì hiển thị lên giao diện
        android.content.Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("name");
            String description = intent.getStringExtra("description");
            String note = intent.getStringExtra("note");
            String imageUrl = intent.getStringExtra("imageUrl");
            // Lấy menu_id từ Intent (hỗ trợ cả "menu_id" và "menuId" để tương thích)
            menuId = intent.getStringExtra("menu_id");
            if (menuId == null || menuId.isEmpty()) {
                // Backwards compatibility: some places used camelCase key
                menuId = intent.getStringExtra("menuId");
            }
            etRecipeName.setText(name != null ? name : "");
            etRecipeDescription.setText(description != null ? description : "");
            etRecipeNote.setText(note != null ? note : "");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_food_placeholder)
                        .error(R.drawable.ic_food_placeholder)
                        .into(imageRecipePreview);
            } else {
                imageRecipePreview.setImageResource(R.drawable.ic_food_placeholder);
            }
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
        String description = etRecipeDescription.getText().toString().trim();
        String note = etRecipeNote.getText().toString().trim();
        String imageUrl = (imageUri != null) ? imageUri.toString() : null;
        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (menuId == null || menuId.isEmpty()) {
            Toast.makeText(this, "Menu ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }
        dbExecutor.execute(() -> {
            try (Connection conn = com.example.final_project.utils.DatabaseConnection.getConnection()) {
                if (conn == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Database connection failed!", Toast.LENGTH_SHORT).show());
                    return;
                }
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
                    stmt.setString(3, description);
                    stmt.setString(4, note);
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
                    // If this activity was started with startActivityForResult, return OK so caller can reload
                    if (getCallingActivity() != null) {
                        setResult(Activity.RESULT_OK);
                        finish();
                        return;
                    }

                    // Otherwise, explicitly navigate to RecipeListActivity for this menu so user sees the recipes in the menu
                    try {
                        android.content.Intent intent = new android.content.Intent(CreateRecipeActivity.this, com.example.final_project.views.activity.RecipeListActivity.class);
                        intent.putExtra("menu_id", menuId);
                        // If a RecipeListActivity already exists in the task, bring it to front instead of creating a new duplicate
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    } catch (Exception ex) {
                        // Fallback: just finish and let caller handle
                        android.util.Log.w("CreateRecipeActivity", "Could not start RecipeListActivity, falling back to finish()", ex);
                    }
                    finish();
                });
            } catch (Exception e) {
                android.util.Log.e("CreateRecipeActivity", "Error saving recipe", e);
                runOnUiThread(() -> Toast.makeText(this, "Error saving recipe!", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
