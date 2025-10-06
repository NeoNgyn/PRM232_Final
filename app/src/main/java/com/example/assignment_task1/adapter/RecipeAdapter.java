package com.example.assignment_task1.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.assignment_task1.R;
import com.example.assignment_task1.model.Recipe;
import java.io.InputStream;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes;

    public RecipeAdapter(List<Recipe> recipes) {
        this.recipes = recipes;
        android.util.Log.d("RecipeAdapter", "Created adapter with " + recipes.size() + " recipes");
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        android.util.Log.d("RecipeAdapter", "Binding recipe " + position + ": " + recipe.getName());
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        int count = recipes.size();
        android.util.Log.d("RecipeAdapter", "getItemCount: " + count);
        return count;
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageRecipe;
        private TextView textRecipeName;
        private TextView textRecipeDescription;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRecipe = itemView.findViewById(R.id.image_recipe);
            textRecipeName = itemView.findViewById(R.id.text_recipe_name);
            textRecipeDescription = itemView.findViewById(R.id.text_recipe_description);
        }

        public void bind(Recipe recipe) {
            textRecipeName.setText(recipe.getName());
            textRecipeDescription.setText(recipe.getDescription());
            // Loại bỏ hậu tố _1 nếu có trong tên file
            String imageFileName = recipe.getImageName1();
            if (imageFileName.endsWith("_1")) {
                imageFileName = imageFileName.substring(0, imageFileName.length() - 2);
            }
            boolean loaded = false;
            String[] extensions = {"", ".jpg", ".png"};
            for (String ext : extensions) {
                try {
                    InputStream is = itemView.getContext().getAssets().open("food_images/" + imageFileName + ext);
                    Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
                    imageRecipe.setImageBitmap(bitmap);
                    is.close();
                    loaded = true;
                    break;
                } catch (Exception e) {
                    // thử tiếp với đuôi khác
                }
            }
            if (!loaded) {
                imageRecipe.setImageResource(R.drawable.ic_food_placeholder);
                android.util.Log.e("RecipeAdapter", "Không tìm thấy hoặc đọc được ảnh: food_images/" + imageFileName + "(.jpg/.png)");
            }
        }
    }
}
