package com.example.final_project.views.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.models.entity.RecipeInMenu;
import com.google.android.material.button.MaterialButton;

import java.io.InputStream;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<RecipeInMenu> recipes;
    private OnRecipeActionListener actionListener;

    public interface OnRecipeActionListener {
        void onDeleteRecipe(RecipeInMenu recipeInMenu, int position);
        void onEditRecipe(RecipeInMenu recipeInMenu, int position);
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
                    if (actionListener != null) {
                        actionListener.onDeleteRecipe(recipeInMenu, pos);
                    }
                }
            });
            holder.btnEdit.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (actionListener != null) {
                        actionListener.onEditRecipe(recipeInMenu, pos);
                    } else if (recipeInMenu.getRecipe() != null) {
                        android.content.Context ctx = holder.itemView.getContext();
                        android.content.Intent intent = new android.content.Intent(ctx, com.example.final_project.views.activity.CreateRecipeActivity.class);
                        intent.putExtra("recipe", (java.io.Serializable) recipeInMenu.getRecipe());
                        if (recipeInMenu.getMenu() != null && recipeInMenu.getMenu().getMenuId() != null) {
                            intent.putExtra("menu_id", recipeInMenu.getMenu().getMenuId());
                        }
                        ctx.startActivity(intent);
                    }
                }
            });
        } else {
            holder.btnDelete.setOnClickListener(null);
        }
        holder.btnEdit.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && recipeInMenu.getRecipe() != null) {
                android.content.Context ctx = holder.itemView.getContext();
                android.content.Intent intent = new android.content.Intent(ctx, com.example.final_project.views.activity.CreateRecipeActivity.class);
                intent.putExtra("recipe", (java.io.Serializable) recipeInMenu.getRecipe());
                if (recipeInMenu.getMenu() != null && recipeInMenu.getMenu().getMenuId() != null) {
                    intent.putExtra("menu_id", recipeInMenu.getMenu().getMenuId());
                }
                ctx.startActivity(intent);
            }
        });
        // Thêm sự kiện click cho itemView để mở RecipeDetailActivity
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && recipeInMenu.getRecipe() != null) {
                android.content.Context ctx = holder.itemView.getContext();
                android.content.Intent intent = new android.content.Intent(ctx, com.example.final_project.views.activity.MealDetailActivity.class);
                intent.putExtra("recipe", (java.io.Serializable) recipeInMenu.getRecipe());
                ctx.startActivity(intent);
            }
        });
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
        MaterialButton btnDelete;
        MaterialButton btnEdit;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRecipe = itemView.findViewById(R.id.image_recipe);
            textRecipeName = itemView.findViewById(R.id.text_recipe_name);
            textRecipeDescription = itemView.findViewById(R.id.text_recipe_description);
            btnDelete = itemView.findViewById(R.id.btn_delete_recipe);
            btnEdit = itemView.findViewById(R.id.btn_edit_recipe);
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
            String imageUrl = recipe.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    android.content.Context ctx = itemView.getContext();
                    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                        // Web URL
                        com.bumptech.glide.Glide.with(ctx)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_food_placeholder)
                                .error(R.drawable.ic_food_placeholder)
                                .into(imageRecipe);
                    } else if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                        // Content URI or file URI
                        android.net.Uri uri = android.net.Uri.parse(imageUrl);
                        com.bumptech.glide.Glide.with(ctx)
                                .load(uri)
                                .placeholder(R.drawable.ic_food_placeholder)
                                .error(R.drawable.ic_food_placeholder)
                                .into(imageRecipe);
                    } else if (imageUrl.startsWith("/storage") || imageUrl.startsWith("/data") || imageUrl.startsWith("/mnt")) {
                        // Absolute file path
                        java.io.File f = new java.io.File(imageUrl);
                        if (f.exists()) {
                            com.bumptech.glide.Glide.with(ctx)
                                    .load(f)
                                    .placeholder(R.drawable.ic_food_placeholder)
                                    .error(R.drawable.ic_food_placeholder)
                                    .into(imageRecipe);
                        } else {
                            // Fallback to placeholder
                            imageRecipe.setImageResource(R.drawable.ic_food_placeholder);
                        }
                    } else {
                        // Try to parse as URI, fallback to placeholder
                        try {
                            android.net.Uri uri = android.net.Uri.parse(imageUrl);
                            com.bumptech.glide.Glide.with(ctx)
                                    .load(uri)
                                    .placeholder(R.drawable.ic_food_placeholder)
                                    .error(R.drawable.ic_food_placeholder)
                                    .into(imageRecipe);
                        } catch (Exception e) {
                            imageRecipe.setImageResource(R.drawable.ic_food_placeholder);
                        }
                    }
                } catch (Exception ex) {
                    android.util.Log.w("RecipeAdapter", "Failed to load image: " + imageUrl, ex);
                    imageRecipe.setImageResource(R.drawable.ic_food_placeholder);
                }
            } else {
                imageRecipe.setImageResource(R.drawable.ic_food_placeholder);
            }
        }
    }
}
