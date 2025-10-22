package com.example.final_project.views.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;

public class RecipeDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        TextView tvName = findViewById(R.id.tvRecipeName);
        TextView tvInstruction = findViewById(R.id.tvRecipeInstruction);
        TextView tvNutrition = findViewById(R.id.tvRecipeNutrition);
        ImageView ivImage = findViewById(R.id.ivRecipeImage);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        if (recipe != null) {
            tvName.setText(recipe.getName());
            tvInstruction.setText(recipe.getInstruction());
            tvNutrition.setText(recipe.getNutrition());
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
        }
    }
}
