package com.example.final_project.views.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.models.entity.RecipeInMenu;
import com.example.final_project.utils.DatabaseConnection;
import com.example.final_project.views.adapter.RecipeAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeListActivity extends AppCompatActivity {
    private RecyclerView recyclerViewRecipe;
    private RecipeAdapter recipeAdapter;
    private List<RecipeInMenu> recipeList;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private static final int REQUEST_ADD_RECIPE = 1001;
    private String currentMenuId;

    // Lưu padding-top gốc của header để không cộng dồn khi onResume
    private int headerOriginalPaddingTop = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // Đảm bảo header luôn ở trên cùng và không bị che bởi status bar
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

        recyclerViewRecipe = findViewById(R.id.recyclerViewRecipe);
        recyclerViewRecipe.setLayoutManager(new LinearLayoutManager(this));
        recipeList = new ArrayList<>();
        // Create adapter with delete and edit action callback
        recipeAdapter = new RecipeAdapter(recipeList, new RecipeAdapter.OnRecipeActionListener() {
            @Override
            public void onDeleteRecipe(RecipeInMenu recipeInMenu, int position) {
                new AlertDialog.Builder(RecipeListActivity.this)
                        .setTitle("Xóa công thức")
                        .setMessage("Bạn có chắc muốn xoá không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            String recipeMenuId = recipeInMenu.getRecipeMenuId();
                            deleteRecipeInMenu(recipeMenuId, currentMenuId, recipeInMenu);
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .show();
            }
            @Override
            public void onEditRecipe(RecipeInMenu recipeInMenu, int position) {
                if (recipeInMenu.getRecipe() != null) {
                    android.content.Intent intent = new android.content.Intent(RecipeListActivity.this, CreateRecipeActivity.class);
                    intent.putExtra("recipe", (java.io.Serializable) recipeInMenu.getRecipe());
                    if (recipeInMenu.getMenu() != null && recipeInMenu.getMenu().getMenuId() != null) {
                        intent.putExtra("menu_id", recipeInMenu.getMenu().getMenuId());
                    } else if (currentMenuId != null) {
                        intent.putExtra("menu_id", currentMenuId);
                    }
                    startActivity(intent);
                }
            }
        });
        recyclerViewRecipe.setAdapter(recipeAdapter);

        // Thêm xử lý nút back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        String menuId = getIntent().getStringExtra("menu_id");
        currentMenuId = menuId;
        if (menuId == null || menuId.isEmpty()) {
            Toast.makeText(this, "No menu selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadRecipes(menuId);

        // Thêm xử lý FloatingActionButton để mở CreateRecipeActivity và truyền menu_id
        findViewById(R.id.fabAddRecipe).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(RecipeListActivity.this, CreateRecipeActivity.class);
            intent.putExtra("menu_id", menuId);
            startActivityForResult(intent, REQUEST_ADD_RECIPE);
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

    /**
     * Delete mapping (RecipeInMenu). If recipeMenuId is null/empty, try delete by recipe_id + menu_id
     */
    private void deleteRecipeInMenu(String recipeMenuId, String menuId, RecipeInMenu recipeInMenu) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    runOnUiThread(() -> Toast.makeText(RecipeListActivity.this, "Database connection failed!", Toast.LENGTH_SHORT).show());
                    return;
                }
                int affected = 0;
                if (recipeMenuId != null && !recipeMenuId.isEmpty()) {
                    String sql = "DELETE FROM RecipeInMenu WHERE recipeMenu_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, recipeMenuId);
                        affected = stmt.executeUpdate();
                    }
                } else if (recipeInMenu != null && recipeInMenu.getRecipe() != null && menuId != null) {
                    String sql = "DELETE FROM RecipeInMenu WHERE recipe_id = ? AND menu_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, recipeInMenu.getRecipe().getRecipeId());
                        stmt.setString(2, menuId);
                        affected = stmt.executeUpdate();
                    }
                }
                final int deleted = affected;
                runOnUiThread(() -> {
                    if (deleted > 0) {
                        Toast.makeText(RecipeListActivity.this, "Đã xóa recipe.", Toast.LENGTH_SHORT).show();
                        // refresh list
                        if (menuId != null && !menuId.isEmpty()) {
                            loadRecipes(menuId);
                        }
                    } else {
                        Toast.makeText(RecipeListActivity.this, "Xóa không thành công.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("RecipeListActivity", "Error deleting recipeInMenu", e);
                runOnUiThread(() -> Toast.makeText(RecipeListActivity.this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_RECIPE && resultCode == Activity.RESULT_OK) {
            // reload recipes for current menu
            String menuId = getIntent().getStringExtra("menu_id");
            if (menuId != null && !menuId.isEmpty()) {
                loadRecipes(menuId);
            }
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        // Update the stored intent so getIntent() returns the latest one
        setIntent(intent);
        String menuId = intent.getStringExtra("menu_id");
        if (menuId == null || menuId.isEmpty()) {
            menuId = currentMenuId;
        }
        if (menuId != null && !menuId.isEmpty()) {
            currentMenuId = menuId;
            loadRecipes(menuId);
        }
    }

    private void loadRecipes(String menuId) {
        dbExecutor.execute(() -> {
            final List<RecipeInMenu> loadedList = new ArrayList<>();
            final String[] errorMessage = {null}; // To hold error message from background thread

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Check if connection is null
                if (conn == null) {
                    errorMessage[0] = "Failed to connect to the database.";
                } else {
                    String sql = "SELECT r.recipe_id, r.name, r.instruction, r.nutrition, r.image_url, r.create_at, r.update_at, rm.recipeMenu_id FROM Recipe r JOIN RecipeInMenu rm ON r.recipe_id = rm.recipe_id WHERE rm.menu_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, menuId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                Recipe recipe = new Recipe(
                                        rs.getString("recipe_id"),
                                        rs.getString("name"),
                                        rs.getString("instruction"),
                                        rs.getString("nutrition"),
                                        rs.getString("image_url"),
                                        rs.getDate("create_at"),
                                        rs.getDate("update_at")
                                );
                                android.util.Log.d("RecipeListActivity", "Loaded recipe image_url=" + rs.getString("image_url") + " for recipe_id=" + rs.getString("recipe_id"));
                                RecipeInMenu recipeInMenu = new RecipeInMenu(
                                        rs.getString("recipeMenu_id"),
                                        recipe,
                                        null // menu không cần thiết ở đây
                                );
                                loadedList.add(recipeInMenu);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Log the full exception stack trace for debugging
                Log.e("RecipeListActivity", "Error loading recipes", e);
                errorMessage[0] = "An error occurred while loading recipes.";
            }

            // Switch back to the main thread to update the UI
            runOnUiThread(() -> {
                if (errorMessage[0] != null) {
                    // If there was an error, show it and don't change the list
                    Toast.makeText(RecipeListActivity.this, errorMessage[0], Toast.LENGTH_LONG).show();
                } else {
                    // If successful, update the adapter's data
                    recipeList.clear();
                    recipeList.addAll(loadedList);
                    recipeAdapter.notifyDataSetChanged();
                    if (loadedList.isEmpty()) {
                        Toast.makeText(RecipeListActivity.this, "No recipes found for this menu.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}
