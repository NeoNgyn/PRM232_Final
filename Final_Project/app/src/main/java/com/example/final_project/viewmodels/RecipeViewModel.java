//// File: viewmodels/RecipeViewModel.java
//package com.example.final_project.viewmodels;
//
//import android.util.Log;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//import com.example.final_project.models.entity.Ingredient;
//import com.example.final_project.models.entity.Recipe;
//import com.example.final_project.models.entity.RecipeData;
//import com.example.final_project.models.repository.RecipeRepository;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//import android.os.Handler;
//import android.os.Looper;
//
//public class RecipeViewModel extends ViewModel {
//
//    private RecipeRepository recipeRepository;
//    private final MutableLiveData<Boolean> _saveStatus = new MutableLiveData<>();
//    public LiveData<Boolean> saveStatus = _saveStatus;
//
//    public RecipeViewModel() {
//        // Không cần Application context
//        recipeRepository = new RecipeRepository();
//    }
//    public void resetSaveStatus() {
//        _saveStatus.setValue(null);
//    }
//
//    public void saveRecipeToDatabase(RecipeData recipeData, String userId) {
//        Log.d("RecipeViewModel", "Bắt đầu lưu món ăn: " + recipeData.getTitle());
//        Handler mainHandler = new Handler(Looper.getMainLooper());
//
//        try {
//            // --- THAY ĐỔI 1: TẠO RECIPE ID NGẮN HƠN ---
//            // Sử dụng timestamp để tạo ID có độ dài khoảng 10 ký tự, ví dụ: "R1666198800"
//            String newRecipeId = "R" + (System.currentTimeMillis() / 1000L) % 1000000000L;
//
//            Recipe newRecipe = new Recipe(
//                    newRecipeId,
//                    recipeData.getTitle(),
//                    recipeData.getInstructions(),
//                    recipeData.getNutrition(), // <-- Lấy nutrition từ RecipeData
//                    null,
//                    new Date(),
//                    new Date()
//            );
//
//            List<Ingredient> ingredientsList = new ArrayList<>();
//            String[] ingredientsArray = recipeData.getIngredients().split(",\\s*");
//
//            for (String ingredientName : ingredientsArray) {
//                if (!ingredientName.trim().isEmpty()) {
//                    Ingredient ingredient = new Ingredient();
//
//                    // --- THAY ĐỔI 2: TẠO INGREDIENT ID NGẮN HƠN ---
//                    // Thêm `ingredientsList.size()` để tránh trùng ID nếu lưu quá nhanh
//                    String newIngredientId = "I" + ((System.currentTimeMillis() / 1000L) % 1000000000L + ingredientsList.size());
//                    ingredient.setIngredientId(newIngredientId);
//
//                    ingredient.setRecipeId(newRecipeId);
//                    ingredient.setAmount(ingredientName.trim());
//                    ingredientsList.add(ingredient);
//                }
//            }
//
//            recipeRepository.insertRecipe(newRecipe, ingredientsList,
//                    () -> mainHandler.post(() -> _saveStatus.setValue(true)),
//                    (error) -> mainHandler.post(() -> _saveStatus.setValue(false))
//            );
//
//        } catch (Exception e) {
//            Log.e("RecipeViewModel", "Lỗi khi chuẩn bị dữ liệu lưu món ăn", e);
//            mainHandler.post(() -> _saveStatus.setValue(false));
//        }
//    }
//}
// File: viewmodels/RecipeViewModel.java
package com.example.final_project.viewmodels;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.final_project.models.entity.Ingredient;
import com.example.final_project.models.entity.Recipe; // Đảm bảo import đúng Recipe entity
import com.example.final_project.models.entity.RecipeData;
import com.example.final_project.models.repository.RecipeRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import android.os.Handler;
import android.os.Looper;

public class RecipeViewModel extends ViewModel {

    private RecipeRepository recipeRepository;
    private final MutableLiveData<Boolean> _saveStatus = new MutableLiveData<>();
    public LiveData<Boolean> saveStatus = _saveStatus;
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Thêm Handler nếu chưa có

    public RecipeViewModel() {
        recipeRepository = new RecipeRepository();
    }

    public void resetSaveStatus() {
        _saveStatus.setValue(null);
    }

    // Hàm saveRecipeToDatabase nhận RecipeData
    public void saveRecipeToDatabase(RecipeData recipeData, String userId) {
        Log.d("RecipeViewModel", "Bắt đầu lưu món ăn: " + recipeData.getTitle() + " cho User ID: " + userId); // Log thêm userId

        try {
            String newRecipeId = "R" + (System.currentTimeMillis() / 1000L) % 1000000000L;

            // 1. Tạo đối tượng Recipe
            Recipe newRecipe = new Recipe(
                    newRecipeId,
                    recipeData.getTitle(),
                    recipeData.getInstructions(),
                    recipeData.getNutrition(),
                    null, // imageUrl
                    new Date(), // createdAt
                    new Date()  // updatedAt
            );

            // --- ***ĐÂY LÀ PHẦN CHỈNH SỬA QUAN TRỌNG*** ---
            // 2. Gán userId cho đối tượng Recipe (Giả định Recipe entity đã có setUserId)
            newRecipe.setUserId(userId);
            // ---------------------------------------------

            // 3. Tạo danh sách Ingredient (logic giữ nguyên)
            List<Ingredient> ingredientsList = new ArrayList<>();
            // Kiểm tra null hoặc rỗng cho ingredients string
            String ingredientsRaw = recipeData.getIngredients();
            if (ingredientsRaw != null && !ingredientsRaw.isEmpty() && !ingredientsRaw.equalsIgnoreCase("Không có thông tin")) {
                String[] ingredientsArray = ingredientsRaw.split(",\\s*");
                for (String ingredientName : ingredientsArray) {
                    if (!ingredientName.trim().isEmpty()) {
                        Ingredient ingredient = new Ingredient();
                        String newIngredientId = "I" + ((System.currentTimeMillis() / 1000L) % 1000000000L + ingredientsList.size());
                        ingredient.setIngredientId(newIngredientId);
                        ingredient.setRecipeId(newRecipeId);
                        ingredient.setAmount(ingredientName.trim());
                        ingredientsList.add(ingredient);
                    }
                }
            }


            // 4. Gọi Repository để lưu (truyền newRecipe đã có userId)
            recipeRepository.insertRecipe(newRecipe, ingredientsList,
                    () -> mainHandler.post(() -> _saveStatus.setValue(true)),
                    (error) -> mainHandler.post(() -> _saveStatus.setValue(false))
            );

            Log.d("RecipeViewModel", "Đã gửi yêu cầu lưu Recipe entity: " + newRecipeId + " với userId: " + userId);


        } catch (Exception e) {
            Log.e("RecipeViewModel", "Lỗi khi chuẩn bị dữ liệu lưu món ăn", e);
            mainHandler.post(() -> _saveStatus.setValue(false));
        }
    }
}