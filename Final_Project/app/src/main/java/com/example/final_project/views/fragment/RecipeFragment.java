package com.example.final_project.views.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.models.entity.Menu;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.views.adapter.MenuAdapter;
import com.example.final_project.models.entity.RecipeInMenu;
import com.example.final_project.utils.DatabaseConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecipeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MenuAdapter menuAdapter;
    private List<Menu> weeklyMenu;

    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);

        initViews(view);
        setupRecyclerView();
        loadWeeklyMenuFromDatabase();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_menu);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        weeklyMenu = new ArrayList<>();
        menuAdapter = new MenuAdapter(weeklyMenu);
        recyclerView.setAdapter(menuAdapter);
    }

    private void loadWeeklyMenuFromDatabase() {
        dbExecutor.execute(() -> {
            List<Menu> menus = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                // Load menus with their recipes
                String sql = "SELECT m.menu_id, m.menu_name, m.image_url, m.description, " +
                           "r.recipe_id, r.name, r.instruction, r.nutrition, r.image_url as recipe_image_url " +
                           "FROM Menu m " +
                           "LEFT JOIN RecipeInMenu rim ON m.menu_id = rim.menu_id " +
                           "LEFT JOIN Recipe r ON rim.recipe_id = r.recipe_id " +
                           "ORDER BY m.create_at DESC, m.menu_id";

                try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                    Menu currentMenu = null;
                    String currentMenuId = null;

                    while (rs.next()) {
                        String menuId = rs.getString("menu_id");
                        if (!menuId.equals(currentMenuId)) {
                            // New menu
                            if (currentMenu != null) {
                                menus.add(currentMenu);
                            }
                            String menuName = rs.getString("menu_name");
                            String imageUrl = rs.getString("image_url");
                            String description = rs.getString("description");
                            currentMenu = new Menu(menuId, menuName, imageUrl, description, null, null, null, null);
                            currentMenu.setRecipeList(new ArrayList<>());
                            currentMenuId = menuId;
                        }

                        // Add recipe to current menu if exists
                        String recipeId = rs.getString("recipe_id");
                        if (recipeId != null && currentMenu != null) {
                            String recipeName = rs.getString("name");
                            String instruction = rs.getString("instruction");
                            String nutrition = rs.getString("nutrition");
                            String recipeImageUrl = rs.getString("recipe_image_url");

                            Recipe recipe = new Recipe(recipeId, recipeName, instruction, nutrition, recipeImageUrl, null, null);
                            RecipeInMenu recipeInMenu = new RecipeInMenu(null, recipe, currentMenu);
                            currentMenu.getRecipeList().add(recipeInMenu);
                        }
                    }

                    // Add the last menu
                    if (currentMenu != null) {
                        menus.add(currentMenu);
                    }
                }

                getActivity().runOnUiThread(() -> {
                    weeklyMenu.clear();
                    weeklyMenu.addAll(menus);
                    menuAdapter.notifyDataSetChanged();

                    android.util.Log.d("RecipeFragment", "Loaded " + weeklyMenu.size() + " menus from database");
                    for (Menu menu : weeklyMenu) {
                        android.util.Log.d("RecipeFragment", menu.getMenuName() + " has " + (menu.getRecipeList() != null ? menu.getRecipeList().size() : 0) + " recipes");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    // Fallback to mock data if DB fails
                    loadMockDataFallback();
                });
            }
        });
    }

    private void loadMockDataFallback() {
        // Tạo mock data cho menu tuần
        weeklyMenu.clear();

        // Thứ 2
        List<Recipe> mondayRecipes = Arrays.asList(
            new Recipe("Phở Bò", "Phở bò truyền thống với nước dùng đậm đà", "", "pho_bo", "pho_bo_2", null, null),
            new Recipe("Bún Chả", "Bún chả Hà Nội với thịt nướng thơm ngon", "", "bun_cha", "bun_cha_2", null, null),
            new Recipe("Chè Đậu Xanh", "Chè đậu xanh mát lạnh", "", "che_dau_xanh", "che_dau_xanh_2", null, null)
        );
        weeklyMenu.add(new Menu("Thứ Hai", "Thứ Hai", "", "Thứ Hai", null, null, null, null));

        android.util.Log.d("RecipeFragment", "Loaded " + weeklyMenu.size() + " days (fallback)");
        for (Menu dayMenu : weeklyMenu) {
            android.util.Log.d("RecipeFragment", dayMenu.getMenuName() + " has " + dayMenu.getRecipeList().size() + " recipes");
        }

        menuAdapter.notifyDataSetChanged();
    }
}
