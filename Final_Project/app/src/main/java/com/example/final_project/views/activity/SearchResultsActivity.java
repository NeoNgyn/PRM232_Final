package com.example.final_project.views.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.models.entity.RecipeInMenu;
import com.example.final_project.utils.DatabaseConnection;
import com.example.final_project.utils.UserSessionManager;
import com.example.final_project.views.adapter.HomeMenuAdapter;
import com.example.final_project.views.adapter.RecipeAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchResultsActivity extends AppCompatActivity {

    private TextView tvSearchQuery;
    private ImageView btnBack;
    private RecyclerView recyclerViewMenuResults;
    private RecyclerView recyclerViewRecipeResults;
    private LinearLayout layoutNoResults;
    private LinearLayout layoutResults;
    private LinearLayout layoutMenuSection;
    private LinearLayout layoutRecipeSection;

    private HomeMenuAdapter menuAdapter;
    private RecipeAdapter recipeAdapter;
    private List<HomeMenuAdapter.MenuItem> menuList;
    private List<RecipeInMenu> recipeList;

    private String searchQuery;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // Get search query from intent
        searchQuery = getIntent().getStringExtra("search_query");
        if (searchQuery == null) {
            searchQuery = "";
        }
        searchQuery = searchQuery.trim();

        initializeViews();
        setupRecyclerViews();

        // Ensure header is on top and handle status bar
        setupHeader();

        performSearch();
    }

    private void setupHeader() {
        View header = findViewById(R.id.headerContainer);
        if (header != null) {
            // Add status bar padding
            int statusBarHeight = getStatusBarHeight();
            header.setPadding(
                header.getPaddingLeft(),
                header.getPaddingTop() + statusBarHeight,
                header.getPaddingRight(),
                header.getPaddingBottom()
            );

            // Bring to front to ensure it's on top
            header.bringToFront();
            header.requestLayout();
            header.invalidate();
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void initializeViews() {
        tvSearchQuery = findViewById(R.id.tvSearchQuery);
        btnBack = findViewById(R.id.btnBack);
        recyclerViewMenuResults = findViewById(R.id.recyclerViewMenuResults);
        recyclerViewRecipeResults = findViewById(R.id.recyclerViewRecipeResults);
        layoutNoResults = findViewById(R.id.layoutNoResults);
        layoutResults = findViewById(R.id.layoutResults);
        layoutMenuSection = findViewById(R.id.layoutMenuSection);
        layoutRecipeSection = findViewById(R.id.layoutRecipeSection);

        // Show appropriate title based on search query
        if (searchQuery.isEmpty()) {
            tvSearchQuery.setText("Tất cả Menu & Công thức");
        } else {
            tvSearchQuery.setText("Kết quả cho: \"" + searchQuery + "\"");
        }
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        // Setup Menu RecyclerView
        menuList = new ArrayList<>();
        recyclerViewMenuResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMenuResults.setNestedScrollingEnabled(false);
        menuAdapter = new HomeMenuAdapter(this, menuList, new HomeMenuAdapter.OnMenuActionListener() {
            @Override
            public void onEditMenu(HomeMenuAdapter.MenuItem menuItem, int position) {
                // Handle edit if needed
            }

            @Override
            public void onDeleteMenu(HomeMenuAdapter.MenuItem menuItem, int position) {
                // Handle delete if needed
            }
        });
        recyclerViewMenuResults.setAdapter(menuAdapter);

        // Setup Recipe RecyclerView
        recipeList = new ArrayList<>();
        recyclerViewRecipeResults.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewRecipeResults.setNestedScrollingEnabled(false);
        recipeAdapter = new RecipeAdapter(recipeList, new RecipeAdapter.OnRecipeActionListener() {
            @Override
            public void onDeleteRecipe(RecipeInMenu recipeInMenu, int position) {
                // Handle delete if needed
            }

            @Override
            public void onEditRecipe(RecipeInMenu recipeInMenu, int position) {
                // Handle edit if needed
            }
        });
        recyclerViewRecipeResults.setAdapter(recipeAdapter);
    }

    private void performSearch() {
        String currentUserId = UserSessionManager.getInstance(this).getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbExecutor.execute(() -> {
            List<HomeMenuAdapter.MenuItem> foundMenus = new ArrayList<>();
            List<RecipeInMenu> foundRecipes = new ArrayList<>();

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                // Search for Menus - Only search by menu_name
                String menuSql;
                if (searchQuery.isEmpty()) {
                    // Show all menus when search is empty
                    menuSql = "SELECT menu_id, menu_name, image_url, description, from_date, to_date " +
                            "FROM Menu " +
                            "WHERE user_id = ? " +
                            "ORDER BY create_at DESC";
                } else {
                    // Search only by menu_name (not description)
                    menuSql = "SELECT menu_id, menu_name, image_url, description, from_date, to_date " +
                            "FROM Menu " +
                            "WHERE user_id = ? AND LOWER(menu_name) LIKE ? " +
                            "ORDER BY create_at DESC";
                }

                try (PreparedStatement stmt = conn.prepareStatement(menuSql)) {
                    stmt.setString(1, currentUserId);
                    if (!searchQuery.isEmpty()) {
                        String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                        stmt.setString(2, searchPattern);
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            HomeMenuAdapter.MenuItem item = new HomeMenuAdapter.MenuItem(
                                    rs.getString("menu_id"),
                                    rs.getString("menu_name"),
                                    rs.getString("image_url"),
                                    rs.getString("description"),
                                    rs.getString("from_date"),
                                    rs.getString("to_date")
                            );
                            foundMenus.add(item);
                        }
                    }
                }

                // Search for Recipes - Only search by recipe name
                String recipeSql;
                if (searchQuery.isEmpty()) {
                    // Load all recipes that belong to the current user (including those not in any menu)
                    recipeSql = "SELECT r.recipe_id, r.name AS recipe_name, r.instruction, " +
                            "r.nutrition, r.image_url, r.create_at " +
                            "FROM Recipe r " +
                            "WHERE r.user_id = ? " +
                            "ORDER BY r.create_at DESC";
                } else {
                    // Search only by recipe name (not instruction)
                    // Include recipes owned by the user or recipes that belong to menus owned by the user
                    recipeSql = "SELECT DISTINCT r.recipe_id, r.name AS recipe_name, r.instruction, " +
                            "r.nutrition, r.image_url, r.create_at " +
                            "FROM Recipe r " +
                            "LEFT JOIN RecipeInMenu rim ON r.recipe_id = rim.recipe_id " +
                            "LEFT JOIN Menu m ON rim.menu_id = m.menu_id " +
                            "WHERE (r.user_id = ? OR m.user_id = ?) AND LOWER(r.name) LIKE ? " +
                            "ORDER BY r.create_at DESC";
                }

                try (PreparedStatement stmt = conn.prepareStatement(recipeSql)) {
                    if (searchQuery.isEmpty()) {
                        stmt.setString(1, currentUserId);
                    } else {
                        // params: 1 = r.user_id, 2 = m.user_id, 3 = searchPattern
                        stmt.setString(1, currentUserId);
                        stmt.setString(2, currentUserId);
                        String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                        stmt.setString(3, searchPattern);
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Recipe recipe = new Recipe();
                            recipe.setRecipeId(rs.getString("recipe_id"));
                            recipe.setName(rs.getString("recipe_name"));
                            recipe.setInstruction(rs.getString("instruction"));
                            recipe.setNutrition(rs.getString("nutrition"));
                            recipe.setImageUrl(rs.getString("image_url"));
                            // Map created_at if available (used for ordering)
                            try {
                                java.sql.Timestamp ts = rs.getTimestamp("create_at");
                                if (ts != null) {
                                    recipe.setCreatedAt(new java.util.Date(ts.getTime()));
                                }
                            } catch (SQLException ignored) {
                                // If column missing for some reason, ignore safely
                            }

                            RecipeInMenu rim = new RecipeInMenu();
                            rim.setRecipe(recipe);
                            foundRecipes.add(rim);
                        }
                    }
                }

                // Update UI on main thread
                runOnUiThread(() -> displaySearchResults(foundMenus, foundRecipes));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(SearchResultsActivity.this, "Lỗi tìm kiếm: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showNoResults();
                });
            }
        });
    }

    private void displaySearchResults(List<HomeMenuAdapter.MenuItem> menus, List<RecipeInMenu> recipes) {
        boolean hasMenus = !menus.isEmpty();
        boolean hasRecipes = !recipes.isEmpty();

        if (!hasMenus && !hasRecipes) {
            showNoResults();
            return;
        }

        // Show results layout
        layoutNoResults.setVisibility(View.GONE);
        layoutResults.setVisibility(View.VISIBLE);

        // Display menus
        if (hasMenus) {
            layoutMenuSection.setVisibility(View.VISIBLE);
            menuList.clear();
            menuList.addAll(menus);
            menuAdapter.notifyDataSetChanged();
        } else {
            layoutMenuSection.setVisibility(View.GONE);
        }

        // Display recipes
        if (hasRecipes) {
            layoutRecipeSection.setVisibility(View.VISIBLE);
            recipeList.clear();
            recipeList.addAll(recipes);
            recipeAdapter.notifyDataSetChanged();
        } else {
            layoutRecipeSection.setVisibility(View.GONE);
        }
    }

    private void showNoResults() {
        layoutNoResults.setVisibility(View.VISIBLE);
        layoutResults.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbExecutor != null && !dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
        }
    }
}
