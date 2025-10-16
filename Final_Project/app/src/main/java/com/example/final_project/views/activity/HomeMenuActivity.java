package com.example.final_project.views.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.utils.DatabaseConnection;
import com.example.final_project.views.adapter.FeaturedFoodAdapter;
import com.example.final_project.views.adapter.HomeMenuAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private RecyclerView recyclerViewFeatured;
    private HomeMenuAdapter menuAdapter;
    private FeaturedFoodAdapter featuredAdapter;
    // FIXED: Use the adapter's own MenuItem class for the list
    private List<HomeMenuAdapter.MenuItem> menuList;
    private List<FeaturedFoodAdapter.FeaturedFood> featuredList;
    private FloatingActionButton fabAddMenu;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_menu);

        fabAddMenu = findViewById(R.id.fabAddMenu);
        fabAddMenu.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                openCreateMenuActivity();
            }
        });

        setupFeaturedRecyclerView();
        setupMenuRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list every time we return to the screen
        loadMenuFromDatabase();
    }

    private void setupFeaturedRecyclerView() {
        recyclerViewFeatured = findViewById(R.id.recyclerViewFeatured);
        recyclerViewFeatured.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFeatured.setHasFixedSize(true);

        featuredList = new ArrayList<>();
        featuredList.add(new FeaturedFoodAdapter.FeaturedFood("Bánh chưng", "banh_chung.jpg"));
        featuredList.add(new FeaturedFoodAdapter.FeaturedFood("Bánh mì", "banh_mi.jpg"));
        featuredList.add(new FeaturedFoodAdapter.FeaturedFood("Thịt kho tàu", "thit_kho_tau.jpg"));
        featuredList.add(new FeaturedFoodAdapter.FeaturedFood("Chả cá", "cha_ca.jpg"));

        featuredAdapter = new FeaturedFoodAdapter(this, featuredList);
        recyclerViewFeatured.setAdapter(featuredAdapter);
    }

    private void setupMenuRecyclerView() {
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMenu.setNestedScrollingEnabled(false);

        menuList = new ArrayList<>();
        menuAdapter = new HomeMenuAdapter(this, menuList, new HomeMenuAdapter.OnMenuActionListener() {
            // FIXED: The method signatures now correctly match the interface
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

    private void loadMenuFromDatabase() {
        dbExecutor.execute(() -> {
            // FIXED: The list is now of the correct type
            List<HomeMenuAdapter.MenuItem> loadedList = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String sql = "SELECT menu_id, menu_name, image_url, description, from_date, to_date FROM Menu ORDER BY create_at DESC";
                try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        // FIXED: Create instances of HomeMenuAdapter.MenuItem
                        HomeMenuAdapter.MenuItem item = new HomeMenuAdapter.MenuItem(
                                rs.getString("menu_id"),
                                rs.getString("menu_name"),
                                rs.getString("image_url"),
                                rs.getString("description"),
                                rs.getString("from_date"),
                                rs.getString("to_date")
                        );
                        loadedList.add(item);
                    }
                }
                runOnUiThread(() -> {
                    menuList.clear();
                    menuList.addAll(loadedList);
                    menuAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(HomeMenuActivity.this, "Error loading menus.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openCreateMenuActivity() {
        Intent intent = new Intent(this, CreateMenuActivity.class);
        startActivity(intent);
    }

    // FIXED: Method now accepts the correct parameter type
    private void showEditMenuDialog(HomeMenuAdapter.MenuItem menuItem) {
        Intent intent = new Intent(this, CreateMenuActivity.class);
        intent.putExtra("menu_id", menuItem.getMenuId());
        // You can still access all the necessary data from the menuItem object
        // intent.putExtra("menu_name", menuItem.getName()); ...etc
        startActivity(intent);
    }

    // FIXED: Method now accepts the correct parameter type
    private void showDeleteMenuDialog(HomeMenuAdapter.MenuItem menuItem, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Menu")
                .setMessage("Are you sure you want to delete '" + menuItem.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMenuFromDb(menuItem.getMenuId(),
                        () -> {
                            menuList.remove(position);
                            menuAdapter.notifyItemRemoved(position);
                            Toast.makeText(this, "Menu deleted successfully", Toast.LENGTH_SHORT).show();
                        },
                        error -> Toast.makeText(this, "Failed to delete menu: " + error, Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMenuFromDb(String menuId, Runnable onSuccess, Consumer<String> onError) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                // Your schema has ON DELETE CASCADE, so this is all that's needed.
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
}