package com.example.final_project.views.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.models.entity.RecipeInMenu;

import java.io.InputStream;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<RecipeInMenu> recipes;
    private OnRecipeActionListener actionListener;

    public interface OnRecipeActionListener {
        void onDeleteRecipe(RecipeInMenu recipeInMenu, int position);
    }

    // Accept an optional action listener
    public RecipeAdapter(List<RecipeInMenu> recipes, OnRecipeActionListener listener) {
        this.recipes = recipes;
        this.actionListener = listener;
        android.util.Log.d("RecipeAdapter", "Created adapter with " + recipes.size() + " recipes");
    }

    // Backwards-compatible constructor
    public RecipeAdapter(List<RecipeInMenu> recipes) {
        this(recipes, null);
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
        RecipeInMenu recipeInMenu = recipes.get(position);
        android.util.Log.d("RecipeAdapter", "Binding recipe " + position + ": " + recipeInMenu.getRecipe());
        if (recipeInMenu.getRecipe() != null) {
            holder.bind(recipeInMenu.getRecipe());
        }
        // Set delete click
        if (actionListener != null) {
            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    actionListener.onDeleteRecipe(recipeInMenu, pos);
                }
            });
        } else {
            holder.btnDelete.setOnClickListener(null);
        }
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
        ImageButton btnDelete;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRecipe = itemView.findViewById(R.id.image_recipe);
            textRecipeName = itemView.findViewById(R.id.text_recipe_name);
            textRecipeDescription = itemView.findViewById(R.id.text_recipe_description);
            btnDelete = itemView.findViewById(R.id.btn_delete_recipe);
        }

        public void bind(Recipe recipe) {
            if (recipe == null) {
                textRecipeName.setText("No recipe");
                textRecipeDescription.setText("");
                imageRecipe.setImageResource(R.drawable.ic_food_placeholder);
                return;
            }
            textRecipeName.setText(recipe.getName() != null ? recipe.getName() : "No name");
            textRecipeDescription.setText(recipe.getInstruction() != null ? recipe.getInstruction() : "");
            // Sử dụng Glide để load ảnh từ image_url
            String imageUrl = recipe.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    android.content.Context ctx = itemView.getContext();
                    // If it's a content:// or file:// URI, parse and load directly
                    if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                        android.net.Uri uri = android.net.Uri.parse(imageUrl);
                        com.bumptech.glide.Glide.with(ctx)
                                .load(uri)
                                .placeholder(R.drawable.ic_food_placeholder)
                                .error(R.drawable.ic_food_placeholder)
                                .into(imageRecipe);
                    } else {
                        // Could be a plain filesystem path (e.g., /storage/emulated/0/...) or a web URL.
                        // Try to detect a local file path first.
                        java.io.File f = new java.io.File(imageUrl);
                        if (f.exists()) {
                            // Load from file (Glide can accept File)
                            com.bumptech.glide.Glide.with(ctx)
                                    .load(f)
                                    .placeholder(R.drawable.ic_food_placeholder)
                                    .error(R.drawable.ic_food_placeholder)
                                    .into(imageRecipe);
                        } else {
                            // Fallback to treating as a URL (http/https)
                            com.bumptech.glide.Glide.with(ctx)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_food_placeholder)
                                    .error(R.drawable.ic_food_placeholder)
                                    .into(imageRecipe);
                        }
                    }
                } catch (Exception ex) {
                    // Fallback to placeholder on any load error
                    android.util.Log.w("RecipeAdapter", "Failed to load image: " + imageUrl, ex);
                    imageRecipe.setImageResource(R.drawable.ic_food_placeholder);
                }
            } else {
                imageRecipe.setImageResource(R.drawable.ic_food_placeholder);
            }
        }
    }
}
