package com.example.final_project.views.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_detail);

        // Get menu ID from intent
        menuId = getIntent().getStringExtra("menu_id");
        if (menuId == null || menuId.isEmpty()) {
            finish(); // Close activity if no menu ID provided
            return;
        }

        recyclerView = findViewById(R.id.recipeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(recipeAdapter);

        // Load menu details and recipes from database
        loadMenuDetails();
    }

    private void loadMenuDetails() {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                // First, load menu details
                String menuSql = "SELECT menu_name, image_url, description FROM Menu WHERE menu_id = ?";
                Menu menu = null;
                try (PreparedStatement menuStmt = conn.prepareStatement(menuSql)) {
                    menuStmt.setString(1, menuId);
                    try (ResultSet rs = menuStmt.executeQuery()) {
                        if (rs.next()) {
                            String menuName = rs.getString("menu_name");
                            String imageUrl = rs.getString("image_url");
                            String description = rs.getString("description");
                            String userId = rs.getString("user_id");
                            menu = new Menu(menuId, menuName, imageUrl, description, null, null, null, null, userId);
                        }
                    }
                }

                // Then, load recipes for this menu
                String recipeSql = "SELECT r.recipe_id, r.name, r.instruction, r.nutrition, r.image_url " +
                                 "FROM Recipe r " +
                                 "INNER JOIN RecipeInMenu rim ON r.recipe_id = rim.recipe_id " +
                                 "WHERE rim.menu_id = ?";
                List<RecipeInMenu> recipes = new ArrayList<>();
                try (PreparedStatement recipeStmt = conn.prepareStatement(recipeSql)) {
                    recipeStmt.setString(1, menuId);
                    try (ResultSet rs = recipeStmt.executeQuery()) {
                        while (rs.next()) {
                            String recipeId = rs.getString("recipe_id");
                            String name = rs.getString("name");
                            String instruction = rs.getString("instruction");
                            String nutrition = rs.getString("nutrition");
                            String imageUrl = rs.getString("image_url");

                            Recipe recipe = new Recipe(recipeId, name, instruction, nutrition, imageUrl, null, null);
                            RecipeInMenu recipeInMenu = new RecipeInMenu(null, recipe, menu);
                            recipes.add(recipeInMenu);
                        }
                    }
                }

                final Menu finalMenu = menu;
                final List<RecipeInMenu> finalRecipes = recipes;
                runOnUiThread(() -> {
                    updateUI(finalMenu, finalRecipes);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Handle error - maybe show a toast or finish activity
                });
            }
        });
    }

    private void updateUI(Menu menu, List<RecipeInMenu> recipes) {
        if (menu != null) {
            // Update menu details in UI
            // Note: You'll need to add TextViews for menu name, description, etc. in menu_detail.xml
            // For now, just update the recipes
        }

        recipeList.clear();
        recipeList.addAll(recipes);
        recipeAdapter.notifyDataSetChanged();
    }
}
