package com.example.final_project.views.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;
import java.util.List;

public class SelectedRecipeAdapter extends RecyclerView.Adapter<SelectedRecipeAdapter.ViewHolder> {

    private final List<Recipe> recipeList;
    private final OnRecipeRemoveListener removeListener;

    public interface OnRecipeRemoveListener {
        void onRemoveRecipe(Recipe recipe, int position);
    }

    public SelectedRecipeAdapter(List<Recipe> recipeList, OnRecipeRemoveListener removeListener) {
        this.recipeList = recipeList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.tvRecipeName.setText(recipe.getName());

        // Load recipe image with Glide
        String imageUrl = recipe.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Check if it's a valid URL
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .centerCrop()
                    .into(holder.ivRecipeImage);
            } else {
                // Legacy filename - show placeholder
                holder.ivRecipeImage.setImageResource(R.drawable.ic_food_placeholder);
            }
        } else {
            // No image - show placeholder
            holder.ivRecipeImage.setImageResource(R.drawable.ic_food_placeholder);
        }

        holder.btnRemoveRecipe.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemoveRecipe(recipe, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRecipeImage;
        TextView tvRecipeName;
        ImageButton btnRemoveRecipe;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            btnRemoveRecipe = itemView.findViewById(R.id.btnRemoveRecipe);
        }
    }
}

