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
import com.example.final_project.views.adapter.HomeMenuAdapter;
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
    private HomeMenuAdapter menuAdapter;
    private HomeMenuAdapter todayMenusAdapter;
    private HomeMenuAdapter upcomingMenusAdapter;
    private List<HomeMenuAdapter.MenuItem> menuList;
    private List<HomeMenuAdapter.MenuItem> todayMenusList;
    private List<HomeMenuAdapter.MenuItem> upcomingMenusList;
    private MaterialButton fabAddMenu;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        fabAddMenu = findViewById(R.id.fabAddMenu);
        fabAddMenu.setOnClickListener(v -> {
            Intent intent = new Intent(HomeMenuActivity.this, CreateMenuActivity.class);
            startActivity(intent);
        });

        setupTodayMenusRecyclerView();
        setupUpcomingMenusRecyclerView();
        setupMenuRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMenuFromDatabase();
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

    private void loadMenuFromDatabase() {
        dbExecutor.execute(() -> {
            List<HomeMenuAdapter.MenuItem> loadedTodayList = new ArrayList<>();
            List<HomeMenuAdapter.MenuItem> loadedUpcomingList = new ArrayList<>();
            List<HomeMenuAdapter.MenuItem> loadedAllList = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());

                String sql = "SELECT menu_id, menu_name, image_url, description, from_date, to_date FROM Menu ORDER BY create_at DESC";
                try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
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

                        if (fromDate != null && toDate != null &&
                            !currentDate.before(fromDate) && !currentDate.after(toDate)) {
                            loadedTodayList.add(item);
                        }
                        else if (fromDate != null && fromDate.after(currentDate)) {
                            loadedUpcomingList.add(item);
                        }

                        loadedAllList.add(item);
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
                    menuList.addAll(loadedAllList);
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
}
