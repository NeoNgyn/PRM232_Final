package com.example.final_project.views.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.models.entity.RecipeInMenu;
import com.example.final_project.models.entity.Menu;
import com.example.final_project.views.adapter.RecipeAdapter;
import com.example.final_project.utils.DatabaseConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class MenuDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<RecipeInMenu> recipeList;
    private String menuId;

    // UI components for menu details
    private ImageView menuImage;
    private TextView menuName;
    private TextView menuDescription;

    // Store menu data for edit/delete
    private String currentMenuName;
    private String currentMenuDescription;
    private String currentMenuImageUrl;
    private String currentFromDate;
    private String currentToDate;

    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_detail);

        // Get menu data from intent
        menuId = getIntent().getStringExtra("menu_id");
        if (menuId == null || menuId.isEmpty()) {
            finish(); // Close activity if no menu ID provided
            return;
        }

        // Initialize views
        menuImage = findViewById(R.id.menuImage);
        menuName = findViewById(R.id.menuName);
        menuDescription = findViewById(R.id.menuDescription);
        recyclerView = findViewById(R.id.recipeRecyclerView);

        // Set vertical layout manager for recipes
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(recipeList, new RecipeAdapter.OnRecipeActionListener() {
            @Override
            public void onDeleteRecipe(RecipeInMenu recipeInMenu, int position) {
                // Show confirmation dialog before deleting
                new androidx.appcompat.app.AlertDialog.Builder(MenuDetailActivity.this)
                        .setTitle("Xóa công thức")
                        .setMessage("Bạn có chắc muốn xóa công thức \"" +
                                   (recipeInMenu.getRecipe() != null ? recipeInMenu.getRecipe().getName() : "") +
                                   "\" khỏi menu không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            deleteRecipeFromMenu(recipeInMenu, position);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onEditRecipe(RecipeInMenu recipeInMenu, int position) {
                // Open CreateRecipeActivity in edit mode
                if (recipeInMenu.getRecipe() != null) {
                    Intent intent = new Intent(MenuDetailActivity.this, CreateRecipeActivity.class);
                    intent.putExtra("recipe", (java.io.Serializable) recipeInMenu.getRecipe());
                    intent.putExtra("menu_id", menuId);
                    startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(recipeAdapter);

        // Setup back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            android.util.Log.d("MenuDetailActivity", "Back button clicked");
            finish();
        });

        // Setup menu options button (3 vertical dots)
        ImageButton btnMenuOptions = findViewById(R.id.btnMenuOptions);
        btnMenuOptions.setOnClickListener(v -> showMenuOptions(v));

        // Check if menu data was passed directly via intent (for faster display)
        String menuNameFromIntent = getIntent().getStringExtra("menu_name");
        String menuDescFromIntent = getIntent().getStringExtra("menu_description");
        String menuImageFromIntent = getIntent().getStringExtra("menu_image_url");
        currentFromDate = getIntent().getStringExtra("from_date");
        currentToDate = getIntent().getStringExtra("to_date");

        // Display menu info immediately if available
        if (menuNameFromIntent != null) {
            menuName.setText(menuNameFromIntent);
            currentMenuName = menuNameFromIntent;
        }
        if (menuDescFromIntent != null) {
            menuDescription.setText(menuDescFromIntent);
            currentMenuDescription = menuDescFromIntent;
        }
        if (menuImageFromIntent != null && !menuImageFromIntent.isEmpty()) {
            currentMenuImageUrl = menuImageFromIntent;
            Glide.with(this)
                .load(menuImageFromIntent)
                .placeholder(R.drawable.banh_mi_1)
                .error(R.drawable.banh_mi_1)
                .into(menuImage);
        }

        // Load menu details and recipes from database (will override if data is different)
        loadMenuDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Skip reload on first load (onCreate already loads data)
        // Only reload when returning from another activity (e.g., after adding a recipe)
        if (!isFirstLoad) {
            android.util.Log.d("MenuDetailActivity", "onResume called - reloading menu details");
            loadMenuDetails();
        } else {
            isFirstLoad = false;
            android.util.Log.d("MenuDetailActivity", "onResume called - skipping first load");
        }
    }

    private void loadMenuDetails() {
        android.util.Log.d("MenuDetailActivity", "Loading menu details for menu_id: " + menuId);
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    android.util.Log.e("MenuDetailActivity", "Database connection is null");
                    throw new SQLException("DB connection is null");
                }

                android.util.Log.d("MenuDetailActivity", "Database connected successfully");

                // First, load menu details (including user_id and dates)
                String menuSql = "SELECT menu_name, image_url, description, user_id, from_date, to_date FROM Menu WHERE menu_id = ?";
                Menu menu = null;
                try (PreparedStatement menuStmt = conn.prepareStatement(menuSql)) {
                    menuStmt.setString(1, menuId);
                    try (ResultSet rs = menuStmt.executeQuery()) {
                        if (rs.next()) {
                            String menuName = rs.getString("menu_name");
                            String imageUrl = rs.getString("image_url");
                            String description = rs.getString("description");
                            String userId = rs.getString("user_id");
                            String fromDate = rs.getString("from_date");
                            String toDate = rs.getString("to_date");

                            // Store data for edit/delete (dates as strings)
                            currentMenuName = menuName;
                            currentMenuDescription = description;
                            currentMenuImageUrl = imageUrl;
                            currentFromDate = fromDate;
                            currentToDate = toDate;

                            // Create Menu object (pass null for dates since Menu constructor expects Date objects)
                            menu = new Menu(menuId, menuName, imageUrl, description, null, null, null, null, userId);
                            android.util.Log.d("MenuDetailActivity", "Menu loaded: " + menuName);
                        } else {
                            android.util.Log.w("MenuDetailActivity", "No menu found with id: " + menuId);
                        }
                    }
                }

                // Then, load recipes for this menu
                android.util.Log.d("MenuDetailActivity", "Loading recipes for menu_id: " + menuId);
                String recipeSql = "SELECT r.recipe_id, r.name, r.instruction, r.nutrition, r.image_url " +
                                 "FROM Recipe r " +
                                 "INNER JOIN RecipeInMenu rim ON r.recipe_id = rim.recipe_id " +
                                 "WHERE rim.menu_id = ?";
                List<RecipeInMenu> recipes = new ArrayList<>();
                try (PreparedStatement recipeStmt = conn.prepareStatement(recipeSql)) {
                    recipeStmt.setString(1, menuId);
                    try (ResultSet rs = recipeStmt.executeQuery()) {
                        int count = 0;
                        while (rs.next()) {
                            count++;
                            String recipeId = rs.getString("recipe_id");
                            String name = rs.getString("name");
                            String instruction = rs.getString("instruction");
                            String nutrition = rs.getString("nutrition");
                            String imageUrl = rs.getString("image_url");

                            Recipe recipe = new Recipe(recipeId, name, instruction, nutrition, imageUrl, null, null);
                            RecipeInMenu recipeInMenu = new RecipeInMenu(null, recipe, menu);
                            recipes.add(recipeInMenu);
                            android.util.Log.d("MenuDetailActivity", "Recipe loaded: " + name + " (id: " + recipeId + ")");
                        }
                        android.util.Log.d("MenuDetailActivity", "Total recipes loaded: " + count);
                    }
                }

                final Menu finalMenu = menu;
                final List<RecipeInMenu> finalRecipes = recipes;
                android.util.Log.d("MenuDetailActivity", "Updating UI with " + recipes.size() + " recipes");
                runOnUiThread(() -> {
                    updateUI(finalMenu, finalRecipes);
                });

            } catch (Exception e) {
                android.util.Log.e("MenuDetailActivity", "Error loading menu details", e);
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Handle error - show toast or finish activity
                    android.widget.Toast.makeText(this, "❌ Lỗi khi load menu: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateUI(Menu menu, List<RecipeInMenu> recipes) {
        android.util.Log.d("MenuDetailActivity", "updateUI called with menu: " + (menu != null ? menu.getMenuName() : "null") + ", recipes: " + recipes.size());

        if (menu != null) {
            // Update menu name
            if (menu.getMenuName() != null && !menu.getMenuName().isEmpty()) {
                menuName.setText(menu.getMenuName());
                android.util.Log.d("MenuDetailActivity", "Set menu name: " + menu.getMenuName());
            }

            // Update menu description
            if (menu.getDescription() != null && !menu.getDescription().isEmpty()) {
                menuDescription.setText(menu.getDescription());
                android.util.Log.d("MenuDetailActivity", "Set menu description: " + menu.getDescription());
            }

            // Update menu image
            if (menu.getImageUrl() != null && !menu.getImageUrl().isEmpty()) {
                // Check if it's a valid URL
                if (menu.getImageUrl().startsWith("http://") || menu.getImageUrl().startsWith("https://")) {
                    android.util.Log.d("MenuDetailActivity", "Loading image from URL: " + menu.getImageUrl());
                    Glide.with(this)
                        .load(menu.getImageUrl())
                        .placeholder(R.drawable.banh_mi_1)
                        .error(R.drawable.banh_mi_1)
                        .into(menuImage);
                } else {
                    // Legacy local filename - show placeholder
                    android.util.Log.w("MenuDetailActivity", "Legacy image filename: " + menu.getImageUrl() + ", showing placeholder");
                    menuImage.setImageResource(R.drawable.banh_mi_1);
                }
            }
        } else {
            android.util.Log.w("MenuDetailActivity", "Menu is null, cannot update menu details");
        }

        // Update recipes list
        recipeList.clear();
        recipeList.addAll(recipes);
        recipeAdapter.notifyDataSetChanged();
        android.util.Log.d("MenuDetailActivity", "Recipe list updated, adapter notified. Total items: " + recipeList.size());
    }

    /**
     * Show popup menu with add recipe, edit and delete options
     */
    private void showMenuOptions(android.view.View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_detail_options, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_add_recipe) {
                addRecipe();
                return true;
            } else if (itemId == R.id.action_edit_menu) {
                editMenu();
                return true;
            } else if (itemId == R.id.action_delete_menu) {
                confirmDeleteMenu();
                return true;
            }
            return false;
        });

        popup.show();
    }

    /**
     * Open CreateRecipeActivity to add new recipe to this menu
     */
    private void addRecipe() {
        android.util.Log.d("MenuDetailActivity", "Add recipe clicked for menu: " + menuId);
        Intent intent = new Intent(this, CreateRecipeActivity.class);
        intent.putExtra("menu_id", menuId);
        intent.putExtra("menu_name", currentMenuName);
        startActivity(intent);
        // Note: Don't finish() here so user comes back to this screen after adding recipe
    }

    /**
     * Open CreateMenuActivity to edit this menu
     */
    private void editMenu() {
        android.util.Log.d("MenuDetailActivity", "Edit menu clicked: " + menuId);
        Intent intent = new Intent(this, CreateMenuActivity.class);
        intent.putExtra("menu_id", menuId);
        intent.putExtra("menu_name", currentMenuName);
        intent.putExtra("description", currentMenuDescription);
        intent.putExtra("image_url", currentMenuImageUrl);
        intent.putExtra("from_date", currentFromDate);
        intent.putExtra("to_date", currentToDate);
        intent.putExtra("is_edit_mode", true);
        startActivity(intent);
        // Finish this activity so when user saves, they go back to home
        finish();
    }

    /**
     * Show confirmation dialog before deleting menu
     */
    private void confirmDeleteMenu() {
        new AlertDialog.Builder(this)
            .setTitle("Xóa menu")
            .setMessage("Bạn có chắc chắn muốn xóa menu \"" + currentMenuName + "\" không?")
            .setPositiveButton("Xóa", (dialog, which) -> deleteMenu())
            .setNegativeButton("Hủy", null)
            .show();
    }

    /**
     * Delete menu from database
     */
    private void deleteMenu() {
        android.util.Log.d("MenuDetailActivity", "Deleting menu: " + menuId);
        Toast.makeText(this, "Đang xóa menu...", Toast.LENGTH_SHORT).show();

        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    runOnUiThread(() -> Toast.makeText(this, "❌ Không thể kết nối database", Toast.LENGTH_SHORT).show());
                    return;
                }

                // First, delete all recipe links in RecipeInMenu
                String sqlDeleteLinks = "DELETE FROM RecipeInMenu WHERE menu_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteLinks)) {
                    stmt.setString(1, menuId);
                    int linksDeleted = stmt.executeUpdate();
                    android.util.Log.d("MenuDetailActivity", "Deleted " + linksDeleted + " recipe links");
                }

                // Then, delete the menu
                String sqlDeleteMenu = "DELETE FROM Menu WHERE menu_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteMenu)) {
                    stmt.setString(1, menuId);
                    int rowsDeleted = stmt.executeUpdate();

                    runOnUiThread(() -> {
                        if (rowsDeleted > 0) {
                            Toast.makeText(this, "✅ Đã xóa menu thành công!", Toast.LENGTH_SHORT).show();
                            android.util.Log.d("MenuDetailActivity", "Menu deleted successfully");
                            // Set result and finish to refresh home screen
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(this, "❌ Không tìm thấy menu để xóa", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (Exception e) {
                android.util.Log.e("MenuDetailActivity", "Error deleting menu", e);
                runOnUiThread(() -> Toast.makeText(this, "❌ Lỗi khi xóa menu: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    /**
     * Delete recipe from menu (remove link in RecipeInMenu table)
     */
    private void deleteRecipeFromMenu(RecipeInMenu recipeInMenu, int position) {
        if (recipeInMenu == null || recipeInMenu.getRecipe() == null) {
            Toast.makeText(this, "❌ Lỗi: Recipe không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String recipeId = recipeInMenu.getRecipe().getRecipeId();
        String recipeName = recipeInMenu.getRecipe().getName();

        android.util.Log.d("MenuDetailActivity", "Deleting recipe " + recipeId + " from menu " + menuId);
        Toast.makeText(this, "Đang xóa công thức...", Toast.LENGTH_SHORT).show();

        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    runOnUiThread(() -> Toast.makeText(this, "❌ Không thể kết nối database", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Delete link in RecipeInMenu table
                String sql = "DELETE FROM RecipeInMenu WHERE recipe_id = ? AND menu_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, recipeId);
                    stmt.setString(2, menuId);
                    int rowsDeleted = stmt.executeUpdate();

                    runOnUiThread(() -> {
                        if (rowsDeleted > 0) {
                            // Remove from list and update UI
                            recipeList.remove(position);
                            recipeAdapter.notifyItemRemoved(position);

                            Toast.makeText(this, "✅ Đã xóa công thức \"" + recipeName + "\" khỏi menu!", Toast.LENGTH_SHORT).show();
                            android.util.Log.d("MenuDetailActivity", "Recipe removed from menu successfully");
                        } else {
                            Toast.makeText(this, "❌ Không tìm thấy công thức để xóa", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (Exception e) {
                android.util.Log.e("MenuDetailActivity", "Error deleting recipe from menu", e);
                runOnUiThread(() -> Toast.makeText(this, "❌ Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
}
