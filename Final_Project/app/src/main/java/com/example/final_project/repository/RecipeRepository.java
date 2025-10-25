package com.example.final_project.repository;

import android.util.Log;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.example.final_project.utils.DatabaseConnection;

public class RecipeRepository {
    // Lấy danh sách nguyên liệu cho một recipe_id
    public static List<String> getIngredientsForRecipe(String recipeId) {
        List<String> ingredients = new ArrayList<>();
        Log.d("RecipeRepository", "Fetching ingredients for recipeId: " + recipeId);
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                Log.e("RecipeRepository", "Database connection is null");
                return ingredients;
            }
            // Fixed: JOIN Unit through FoodItem.unit_id (not Ingredient.unit_id)
            String sql = "SELECT i.amount, fi.food_name, u.unit_name " +
                    "FROM Ingredient i " +
                    "LEFT JOIN FoodItem fi ON i.food_id = fi.food_id " +
                    "LEFT JOIN Unit u ON fi.unit_id = u.unit_id " +
                    "WHERE i.recipe_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, recipeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        String amount = rs.getString("amount");
                        String foodName = rs.getString("food_name");
                        String unitName = rs.getString("unit_name");
                        // Hiển thị dạng: "amount unitName foodName" hoặc "amount foodName" nếu unitName trống
                        String display = (amount != null ? amount + " " : "") +
                                         (unitName != null && !unitName.isEmpty() ? unitName + " " : "") +
                                         (foodName != null ? foodName : "");
                        ingredients.add(display.trim());
                        count++;
                        Log.d("RecipeRepository", "Found ingredient " + count + ": " + display.trim());
                    }
                    Log.d("RecipeRepository", "Total ingredients found: " + count);
                }
            }
        } catch (Exception e) {
            Log.e("RecipeRepository", "Error fetching ingredients for recipeId=" + recipeId, e);
        }
        return ingredients;
    }
}
