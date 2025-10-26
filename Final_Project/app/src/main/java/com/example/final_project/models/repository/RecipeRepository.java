// File: models/repository/RecipeRepository.java
package com.example.final_project.models.repository;

import android.util.Log;
import com.example.final_project.models.dao.RecipeDao;
import com.example.final_project.models.entity.Ingredient;
import com.example.final_project.models.entity.Recipe;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RecipeRepository {
    private final RecipeDao recipeDao;
    private final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public RecipeRepository() {
        // Không cần Application context nữa, chỉ cần khởi tạo DAO
        this.recipeDao = new RecipeDao();
    }

    public void insertRecipe(Recipe recipe, List<Ingredient> ingredients, Runnable onSuccess, Consumer<Exception> onError) {
        databaseWriteExecutor.execute(() -> {
            try {
                recipeDao.insertRecipeWithIngredients(recipe, ingredients);
                // Nếu không có lỗi, gọi callback onSuccess
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (SQLException e) {
                Log.e("RecipeRepository", "Lỗi khi insert recipe", e);
                // Nếu có lỗi, gọi callback onError
                if (onError != null) {
                    onError.accept(e);
                }
            }
        });
    }
}