// File: models/dao/RecipeDao.java
package com.example.final_project.models.dao;

import android.util.Log;
import com.example.final_project.models.entity.Ingredient;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// Chuyển từ interface sang class
public class RecipeDao {

    // Đây là phương thức chính, xử lý transaction
    public void insertRecipeWithIngredients(Recipe recipe, List<Ingredient> ingredients) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                throw new SQLException("Không thể kết nối tới cơ sở dữ liệu.");
            }

            // 1. Bắt đầu transaction
            conn.setAutoCommit(false);

            // 2. Insert vào bảng `Recipe`
            String recipeSql = "INSERT INTO Recipe (recipe_id, name, instruction, nutrition, create_at, update_at, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)"; // <-- Thêm nutrition
            try (PreparedStatement recipeStmt = conn.prepareStatement(recipeSql)) {
                recipeStmt.setString(1, recipe.getRecipeId());
                recipeStmt.setString(2, recipe.getName());
                recipeStmt.setString(3, recipe.getInstruction());
                recipeStmt.setString(4, recipe.getNutrition());
                recipeStmt.setTimestamp(5, new java.sql.Timestamp(recipe.getCreatedAt().getTime()));
                recipeStmt.setTimestamp(6, new java.sql.Timestamp(recipe.getUpdatedAt().getTime()));
                recipeStmt.setString(7, recipe.getUserId());
                recipeStmt.executeUpdate();
            }

            // 3. Insert vào bảng `Ingredient`
            String ingredientSql = "INSERT INTO Ingredient (ingredient_id, recipe_id, amount) VALUES (?, ?, ?)";
            try (PreparedStatement ingredientStmt = conn.prepareStatement(ingredientSql)) {
                for (Ingredient ingredient : ingredients) {
                    ingredientStmt.setString(1, ingredient.getIngredientId());
                    ingredientStmt.setString(2, ingredient.getRecipeId()); // Quan trọng: Liên kết với recipe_id
                    ingredientStmt.setString(3, ingredient.getAmount());   // Lưu tên nguyên liệu cần thêm
                    ingredientStmt.addBatch();
                }
                ingredientStmt.executeBatch();
            }

            // 4. Nếu mọi thứ thành công, commit transaction
            conn.commit();
            Log.d("RecipeDao", "Transaction commited successfully for recipe: " + recipe.getRecipeId());

        } catch (SQLException e) {
            // 5. Nếu có lỗi, rollback tất cả thay đổi
            if (conn != null) {
                try {
                    conn.rollback();
                    Log.e("RecipeDao", "Transaction rolled back.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            // Ném lỗi ra ngoài để Repository và ViewModel biết
            throw e;
        } finally {
            // 6. Đóng kết nối và trả lại trạng thái auto-commit
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}