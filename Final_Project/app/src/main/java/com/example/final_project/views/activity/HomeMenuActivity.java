package com.example.final_project.views.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import com.google.android.material.button.MaterialButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class HomeMenuActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMenu;
    private RecyclerView recyclerViewTodayMenus;
    private RecyclerView recyclerViewUpcomingMenus;
    private RecyclerView recyclerViewRecentRecipes;
    private HomeMenuAdapter menuAdapter;
    private HomeMenuAdapter todayMenusAdapter;
    private HomeMenuAdapter upcomingMenusAdapter;
    private RecipeAdapter recentRecipeAdapter;
    private List<HomeMenuAdapter.MenuItem> menuList;
    private List<HomeMenuAdapter.MenuItem> todayMenusList;
    private List<HomeMenuAdapter.MenuItem> upcomingMenusList;
    private List<RecipeInMenu> recentRecipeList;
    private MaterialButton fabAddMenu;
    private MaterialButton btnGoToFridge;
    private MaterialButton btnMenuNav;
    private MaterialButton btnFridgeNav;
    // Removed btnLogout - now accessed via avatar menu
    private ImageView imgAvatar;
    private ImageView imgSearchIcon;
    private EditText etSearch;

    // Lưu padding-top gốc của header để không cộng dồn khi onResume
    private int headerOriginalPaddingTop = -1;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Đảm bảo header luôn ở trên cùng để dễ bấm
        View header = findViewById(R.id.headerContainer);
        if (header != null) {
            // Lấy padding gốc 1 lần
            if (headerOriginalPaddingTop == -1) {
                headerOriginalPaddingTop = header.getPaddingTop();
            }
            int statusBarHeight = getStatusBarHeight();
            header.setPadding(header.getPaddingLeft(), headerOriginalPaddingTop + statusBarHeight, header.getPaddingRight(), header.getPaddingBottom());

            header.bringToFront();
            header.requestLayout();
            header.invalidate();
        }

        fabAddMenu = findViewById(R.id.fabAddMenu);
        fabAddMenu.setOnClickListener(v -> {
            Intent intent = new Intent(HomeMenuActivity.this, CreateMenuActivity.class);
            startActivity(intent);
        });

        // Initialize navigation buttons
        btnGoToFridge = findViewById(R.id.btnGoToFridge);
        btnMenuNav = findViewById(R.id.btnMenuNav);
        btnFridgeNav = findViewById(R.id.btnFridgeNav);
        // btnLogout removed - now accessed via avatar menu
        imgAvatar = findViewById(R.id.imgAvatar);

        // Setup avatar click listener for popup menu
        imgAvatar.setOnClickListener(v -> showAvatarMenu(v));

        // Setup search functionality
        etSearch = findViewById(R.id.etSearch);
        imgSearchIcon = findViewById(R.id.imgSearchIcon);
        setupSearchFunctionality();

        // Add click listener to search icon with visual feedback
        imgSearchIcon.setOnClickListener(v -> {
            // Hide keyboard when search icon is clicked
            android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
            performSearch();
        });

        // Go to Fridge button
        btnGoToFridge.setOnClickListener(v -> navigateToFridge());

        // Bottom navigation - Menu (already on menu page)
        btnMenuNav.setOnClickListener(v -> {
            Toast.makeText(this, "You are already on Menu page", Toast.LENGTH_SHORT).show();
        });

        // Bottom navigation - Fridge
        btnFridgeNav.setOnClickListener(v -> navigateToFridge());

        // Logout button removed from bottom nav - now in avatar menu

        setupTodayMenusRecyclerView();
        setupUpcomingMenusRecyclerView();
        setupMenuRecyclerView();
        setupRecentRecipesRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMenuFromDatabase();
        loadRecentRecipesFromDatabase();

        // Đảm bảo header luôn nằm trên cùng sau khi resume
        View header = findViewById(R.id.headerContainer);
        if (header != null) {
            // Nếu chưa lưu padding gốc (thường đã lưu ở onCreate), lưu lại
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

    private void setupTodayMenusRecyclerView() {
        recyclerViewTodayMenus = findViewById(R.id.recyclerViewTodayMenus);
        recyclerViewTodayMenus.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTodayMenus.setHasFixedSize(true);

        todayMenusList = new ArrayList<>();
        todayMenusAdapter = new HomeMenuAdapter(this, todayMenusList, new HomeMenuAdapter.OnMenuActionListener() {
            @Override
            public void onEditMenu(HomeMenuAdapter.MenuItem menuItem, int position) {
                showEditMenuDialog(menuItem);
            }

            @Override
            public void onDeleteMenu(HomeMenuAdapter.MenuItem menuItem, int position) {
                showDeleteMenuDialog(menuItem, position);
            }
        });
        recyclerViewTodayMenus.setAdapter(todayMenusAdapter);
    }

    private void setupUpcomingMenusRecyclerView() {
        recyclerViewUpcomingMenus = findViewById(R.id.recyclerViewUpcomingMenus);
        recyclerViewUpcomingMenus.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewUpcomingMenus.setHasFixedSize(true);

        upcomingMenusList = new ArrayList<>();
        upcomingMenusAdapter = new HomeMenuAdapter(this, upcomingMenusList, new HomeMenuAdapter.OnMenuActionListener() {
            @Override
            public void onEditMenu(HomeMenuAdapter.MenuItem menuItem, int position) {
                showEditMenuDialog(menuItem);
            }

            @Override
            public void onDeleteMenu(HomeMenuAdapter.MenuItem menuItem, int position) {
                showDeleteMenuDialog(menuItem, position);
            }
        });
        recyclerViewUpcomingMenus.setAdapter(upcomingMenusAdapter);
    }

    private void setupMenuRecyclerView() {
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMenu.setNestedScrollingEnabled(false);

        menuList = new ArrayList<>();
        menuAdapter = new HomeMenuAdapter(this, menuList, new HomeMenuAdapter.OnMenuActionListener() {
            @Override
            public void onEditMenu(HomeMenuAdapter.MenuItem menuItem, int position) {
                showEditMenuDialog(menuItem);
            }

            @Override
            public void onDeleteMenu(HomeMenuAdapter.MenuItem menuItem, int position) {
                showDeleteMenuDialog(menuItem, position);
            }
        });
        recyclerViewMenu.setAdapter(menuAdapter);
    }

    private void setupRecentRecipesRecyclerView() {
        recyclerViewRecentRecipes = findViewById(R.id.recyclerViewRecentRecipes);
        recyclerViewRecentRecipes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewRecentRecipes.setHasFixedSize(true);
        recentRecipeList = new ArrayList<>();
        recentRecipeAdapter = new RecipeAdapter(recentRecipeList, new RecipeAdapter.OnRecipeActionListener() {
            @Override
            public void onDeleteRecipe(RecipeInMenu recipeInMenu, int position) {
                new androidx.appcompat.app.AlertDialog.Builder(HomeMenuActivity.this)
                        .setTitle("Xóa công thức")
                        .setMessage("Bạn có chắc muốn xoá không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            // Xóa khỏi database
                            deleteRecipeInMenuFromDb(recipeInMenu, () -> {
                                // Xóa khỏi danh sách hiển thị
                                recentRecipeList.remove(position);
                                recentRecipeAdapter.notifyItemRemoved(position);
                                Toast.makeText(HomeMenuActivity.this, "Đã xóa recipe khỏi database.", Toast.LENGTH_SHORT).show();
                            }, error -> {
                                Toast.makeText(HomeMenuActivity.this, "Lỗi khi xóa: " + error, Toast.LENGTH_LONG).show();
                            });
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .show();
            }

            @Override
            public void onEditRecipe(RecipeInMenu recipeInMenu, int position) {
                // Nếu muốn xử lý nút sửa ở trang chủ, thêm logic tại đây
            }
        });
        recyclerViewRecentRecipes.setAdapter(recentRecipeAdapter);
    }

    // Hàm xóa recipe khỏi bảng RecipeInMenu
    private void deleteRecipeInMenuFromDb(RecipeInMenu recipeInMenu, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");
                String recipeMenuId = recipeInMenu.getRecipeMenuId();
                String recipeId = recipeInMenu.getRecipe() != null ? recipeInMenu.getRecipe().getRecipeId() : null;
                int affected = 0;
                // Xóa khỏi RecipeInMenu trước
                if (recipeMenuId != null && !recipeMenuId.isEmpty()) {
                    String sql = "DELETE FROM RecipeInMenu WHERE recipeMenu_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, recipeMenuId);
                        affected = stmt.executeUpdate();
                    }
                } else if (recipeId != null) {
                    String sql = "DELETE FROM RecipeInMenu WHERE recipe_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, recipeId);
                        affected = stmt.executeUpdate();
                    }
                }
                // Sau đó xóa khỏi Recipe
                if (recipeId != null) {
                    String sql = "DELETE FROM Recipe WHERE recipe_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, recipeId);
                        stmt.executeUpdate();
                    }
                }
                if (affected > 0) {
                    runOnUiThread(onSuccess);
                } else {
                    runOnUiThread(() -> onError.accept("Không tìm thấy hoặc xóa không thành công."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> onError.accept(e.getMessage()));
            }
        });
    }

    private void loadMenuFromDatabase() {
        dbExecutor.execute(() -> {
            List<HomeMenuAdapter.MenuItem> loadedTodayList = new ArrayList<>();
            List<HomeMenuAdapter.MenuItem> loadedUpcomingList = new ArrayList<>();
            List<HomeMenuAdapter.MenuItem> loadedAllList = new ArrayList<>();
            try {
                // Check if user is logged in first
                String currentUserId = UserSessionManager.getInstance(HomeMenuActivity.this).getCurrentUserId();
                if (currentUserId == null || currentUserId.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(HomeMenuActivity.this, "User not logged in. Please login again.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(HomeMenuActivity.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                    return;
                }

                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) throw new SQLException("DB connection is null");

                    java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());

                    String sql = "SELECT menu_id, menu_name, image_url, description, from_date, to_date FROM Menu WHERE user_id = ? ORDER BY create_at DESC";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, currentUserId);
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

                                java.sql.Date fromDate = rs.getDate("from_date");
                                java.sql.Date toDate = rs.getDate("to_date");

                                // Chỉ hiển thị menu nằm trong khoảng from_date - to_date
                                if (fromDate != null && toDate != null &&
                                    !currentDate.before(fromDate) && !currentDate.after(toDate)) {
                                    loadedTodayList.add(item);
                                    loadedAllList.add(item); // Chỉ thêm vào danh sách tổng nếu hợp lệ
                                }
                                else if (fromDate != null && fromDate.after(currentDate)) {
                                    loadedUpcomingList.add(item);
                                }
                                // Nếu muốn hiển thị menu đã qua, có thể thêm else if (toDate != null && toDate.before(currentDate))
                            }
                        }
                    }
                }
                runOnUiThread(() -> {
                    todayMenusList.clear();
                    todayMenusList.addAll(loadedTodayList.size() > 4 ? loadedTodayList.subList(0, 4) : loadedTodayList);
                    todayMenusAdapter.notifyDataSetChanged();

                    upcomingMenusList.clear();
                    upcomingMenusList.addAll(loadedUpcomingList.size() > 4 ? loadedUpcomingList.subList(0, 4) : loadedUpcomingList);
                    upcomingMenusAdapter.notifyDataSetChanged();

                    menuList.clear();
                    menuList.addAll(loadedAllList); // Chỉ hiển thị menu hợp lệ
                    menuAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(HomeMenuActivity.this, "Error loading menus.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadRecentRecipesFromDatabase() {
        dbExecutor.execute(() -> {
            List<RecipeInMenu> loadedRecentRecipes = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");
                String sql =
                        "SELECT " +
                        "    m.menu_id," +
                        "    m.menu_name," +
                        "    r.recipe_id," +
                        "    r.name AS recipe_name," +
                        "    r.instruction," +
                        "    r.nutrition," +
                        "    r.image_url," +
                        "    m.from_date," +
                        "    m.to_date " +
                        "FROM RecipeInMenu rim " +
                        "JOIN Menu m ON rim.menu_id = m.menu_id " +
                        "JOIN Recipe r ON rim.recipe_id = r.recipe_id " +
                        "ORDER BY  rim.recipeMenu_id DESC;";
                try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Recipe recipe = new Recipe();
                        recipe.setRecipeId(rs.getString("recipe_id"));
                        recipe.setName(rs.getString("recipe_name"));
                        recipe.setInstruction(rs.getString("instruction"));
                        recipe.setNutrition(rs.getString("nutrition"));
                        recipe.setImageUrl(rs.getString("image_url"));
                        RecipeInMenu rim = new RecipeInMenu();
                        rim.setRecipe(recipe);
                        // Nếu muốn hiển thị thông tin menu, có thể tạo đối tượng Menu và set vào rim
                        loadedRecentRecipes.add(rim);
                    }
                }
                runOnUiThread(() -> {
                    recentRecipeList.clear();
                    recentRecipeList.addAll(loadedRecentRecipes);
                    recentRecipeAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(HomeMenuActivity.this, "Error loading recent recipes.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openCreateMenuActivity() {
        Intent intent = new Intent(this, CreateMenuActivity.class);
        startActivity(intent);
    }

    private void showEditMenuDialog(HomeMenuAdapter.MenuItem menuItem) {
        Intent intent = new Intent(this, CreateMenuActivity.class);
        intent.putExtra("menu_id", menuItem.getMenuId());
        startActivity(intent);
    }

    private void showDeleteMenuDialog(HomeMenuAdapter.MenuItem menuItem, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Menu")
                .setMessage("Are you sure you want to delete '" + menuItem.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMenuFromDb(menuItem.getMenuId(),
                        () -> {
                            Toast.makeText(this, "Menu deleted successfully", Toast.LENGTH_SHORT).show();
                            loadMenuFromDatabase(); // Reload all data
                        },
                        error -> Toast.makeText(this, "Failed to delete menu: " + error, Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMenuFromDb(String menuId, Runnable onSuccess, Consumer<String> onError) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String sql = "DELETE FROM Menu WHERE menu_id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, menuId);
                    if (stmt.executeUpdate() > 0) {
                        runOnUiThread(onSuccess);
                    } else {
                        throw new SQLException("Delete failed. Menu not found.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> onError.accept(e.getMessage()));
            }
        });
    }

    /**
     * Show avatar popup menu with logout option
     */
    private void showAvatarMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_avatar, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_logout) {
                    showLogoutConfirmDialog();
                    return true;
                }
                return false;
            }
        });

        popup.show();
    }

    /**
     * Navigate to FridgeInventoryActivity
     */
    private void navigateToFridge() {
        Intent intent = new Intent(HomeMenuActivity.this, FridgeInventoryActivity.class);
        startActivity(intent);
    }

    /**
     * Setup search functionality with Enter key and action button listener
     */
    private void setupSearchFunctionality() {
        if (etSearch == null) return;

        // Handle Enter key press
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                     event.getAction() == KeyEvent.ACTION_DOWN)) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        // Optional: Add text watcher for real-time search (uncomment if needed)
        /*
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Implement real-time search if needed
            }
        });
        */
    }

    /**
     * Perform search and navigate to search results activity
     */
    private void performSearch() {
        String query = etSearch.getText().toString().trim();

        // If query is empty, pass empty string to show all results
        Intent intent = new Intent(HomeMenuActivity.this, SearchResultsActivity.class);
        intent.putExtra("search_query", query);
        startActivity(intent);
    }

    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Perform logout: clear session and navigate to Login
     */
    private void performLogout() {
        // Clear user session
        UserSessionManager.getInstance(this).clearUserSession();

        // Navigate to Login activity
        Intent intent = new Intent(HomeMenuActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
