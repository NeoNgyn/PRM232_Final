package com.example.final_project.views.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.utils.DatabaseConnection;
import com.example.final_project.repository.RecipeRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class MealDetailActivity extends AppCompatActivity {
    private static final int REQ_EDIT_RECIPE = 1001;

    // promote to fields so result handler can access
    private Recipe recipe;
    private String menuId;
    private Chip chipIngredients;
    private LinearLayout llContentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        menuId = getIntent().getStringExtra("menu_id");

        // Debug logging
        if (recipe != null) {
            android.util.Log.d("MealDetailActivity", "Recipe loaded: " + recipe.getName() + " (ID: " + recipe.getRecipeId() + ")");
        } else {
            android.util.Log.e("MealDetailActivity", "Recipe is NULL!");
        }

        TextView tvName = findViewById(R.id.tv_detail_meal_name);
        TextView tvInfo = findViewById(R.id.tv_detail_info);
        ImageView ivImage = findViewById(R.id.iv_detail_meal_image);
        ImageButton btnBack = findViewById(R.id.btn_back);
        android.view.View btnMore = findViewById(R.id.btn_more_text);
        ChipGroup chipGroup = findViewById(R.id.chip_group);
        Chip chipNutrition = findViewById(R.id.chip_nutrition);
        chipIngredients = findViewById(R.id.chip_ingredients);
        Chip chipInstructions = findViewById(R.id.chip_instructions);
        llContentContainer = findViewById(R.id.ll_content_container);
        Button btnEdit = findViewById(R.id.btn_edit);
        Button btnDelete = findViewById(R.id.btn_delete);

        btnBack.setOnClickListener(v -> finish());

        // Ensure header controls are above the image and easy to tap on all API levels
        try {
            // Bring to front and raise elevation/translationZ as a runtime safety-net
            btnBack.bringToFront();
            btnBack.setTranslationZ(24f);
            if (btnMore != null) {
                btnMore.bringToFront();
                btnMore.setTranslationZ(24f);
                btnMore.setClickable(true);
                btnMore.setFocusable(true);
            }
        } catch (Exception e) {
            Log.w("MealDetailActivity", "Failed to adjust header z-order", e);
        }

        if (recipe != null) {
            tvName.setText(recipe.getName());
            String imageUrl = recipe.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_food_placeholder)
                        .error(R.drawable.ic_food_placeholder)
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.ic_food_placeholder);
            }
            showContent(llContentContainer, "Nutrition", recipe.getNutrition());
            chipNutrition.setChecked(true);
        }

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (recipe == null) return;
            if (checkedId == R.id.chip_nutrition) {
                showContent(llContentContainer, "Nutrition", recipe.getNutrition());
            } else if (checkedId == R.id.chip_ingredients) {
                // Nếu có recipeId thì lấy dữ liệu từ DB qua RecipeRepository trên background thread
                String recipeId = recipe.getRecipeId();
                android.util.Log.d("MealDetailActivity", "Loading ingredients for recipeId: " + recipeId);
                if (recipeId != null && !recipeId.isEmpty()) {
                    // Hiển thị loading nhỏ
                    llContentContainer.removeAllViews();
                    TextView tvLoading = new TextView(this);
                    tvLoading.setText(getString(R.string.loading_ingredients));
                    tvLoading.setPadding(0, 16, 0, 8);
                    llContentContainer.addView(tvLoading);

                    new Thread(() -> {
                        List<String> ingredientsList = RecipeRepository.getIngredientsForRecipe(recipeId);
                        android.util.Log.d("MealDetailActivity", "Received " + ingredientsList.size() + " ingredients");
                        runOnUiThread(() -> showContent(llContentContainer, "Ingredients", ingredientsList));
                    }).start();
                } else {
                    android.util.Log.w("MealDetailActivity", "recipeId is null or empty!");
                    // Fallback: nếu recipe đã chứa ingredients cục bộ
                    Object ingredients = recipe.getIngredients();
                    showContent(llContentContainer, "Ingredients", ingredients);
                }
            } else if (checkedId == R.id.chip_instructions) {
                showContent(llContentContainer, "Instructions", recipe.getInstruction());
            }
        });

        btnEdit.setOnClickListener(v -> {
            if (recipe != null) {
                Intent intent = new Intent(MealDetailActivity.this, CreateRecipeActivity.class);
                intent.putExtra("recipe", recipe);
                if (menuId != null) intent.putExtra("menu_id", menuId);
                // start for result so we can refresh ingredients after editing/saving
                startActivityForResult(intent, REQ_EDIT_RECIPE);
            }
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(MealDetailActivity.this)
                .setTitle("Xóa công thức")
                .setMessage("Bạn có chắc muốn xoá không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteRecipeFromDb(recipe, () -> {
                        String recipeName = recipe != null && recipe.getName() != null ? recipe.getName() : "";
                        Toast.makeText(MealDetailActivity.this, "Đã xóa: " + recipeName, Toast.LENGTH_SHORT).show();
                        finish();
                    }, error -> {
                        Toast.makeText(MealDetailActivity.this, "Lỗi khi xóa: " + error, Toast.LENGTH_LONG).show();
                    });
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_EDIT_RECIPE && resultCode == Activity.RESULT_OK) {
            // After the CreateRecipeActivity saved changes and finished with RESULT_OK,
            // re-load ingredients from DB and update the Ingredients view if it's selected.
            if (recipe != null && recipe.getRecipeId() != null && !recipe.getRecipeId().isEmpty()) {
                final String recipeId = recipe.getRecipeId();
                new Thread(() -> {
                    List<String> ingredientsList = RecipeRepository.getIngredientsForRecipe(recipeId);
                    runOnUiThread(() -> {
                        if (chipIngredients != null && chipIngredients.isChecked()) {
                            showContent(llContentContainer, "Ingredients", ingredientsList);
                        }
                    });
                }).start();
            }
        }
    }

    private void showContent(LinearLayout container, String title, Object content) {
        container.removeAllViews();
        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        tvTitle.setPadding(0, 16, 0, 8);
        container.addView(tvTitle);

        TextView tvContent = new TextView(this);
        String displayText;
        if (content == null) {
            displayText = getString(R.string.no_data);
        } else if (content instanceof List) {
            List<?> list = (List<?>) content;
            displayText = list.isEmpty() ? getString(R.string.no_data) : android.text.TextUtils.join("\n", list);
        } else if (content.getClass().isArray()) {
            Object[] arr = (Object[]) content;
            displayText = arr.length == 0 ? getString(R.string.no_data) : android.text.TextUtils.join("\n", arr);
        } else {
            displayText = content.toString();
        }
        tvContent.setText(displayText);
        tvContent.setTextSize(16);
        tvContent.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        container.addView(tvContent);
    }

    // Hàm xóa recipe khỏi cả RecipeInMenu và Recipe
    private void deleteRecipeFromDb(Recipe recipe, Runnable onSuccess, Consumer<String> onError) {
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");
                String recipeId = recipe != null ? recipe.getRecipeId() : null;
                int affected = 0;
                // Xóa khỏi RecipeInMenu
                if (recipeId != null) {
                    String sql = "DELETE FROM RecipeInMenu WHERE recipe_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, recipeId);
                        affected = stmt.executeUpdate();
                    }
                }
                // Xóa khỏi Recipe
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
                Log.e("MealDetailActivity", "Error deleting recipe", e);
                runOnUiThread(() -> onError.accept(e.getMessage()));
            }
        }).start();
    }
}
