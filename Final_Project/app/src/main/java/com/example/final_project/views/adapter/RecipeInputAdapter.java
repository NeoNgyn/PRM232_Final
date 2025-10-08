package com.example.final_project.views.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;
import java.util.List;

public class RecipeInputAdapter extends RecyclerView.Adapter<RecipeInputAdapter.ViewHolder> {

    private List<Recipe> recipes;

    public RecipeInputAdapter(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_input, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.editRecipeName.setText(recipe.getName());
        holder.editRecipeDescription.setText(recipe.getDescription());

        holder.btnChooseImage.setOnClickListener(v -> {
            // TODO: mở file picker / gallery
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText editRecipeName, editRecipeDescription;
        ImageView imageRecipe;
        Button btnChooseImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editRecipeName = itemView.findViewById(R.id.editRecipeName);
            editRecipeDescription = itemView.findViewById(R.id.editRecipeDescription);
            imageRecipe = itemView.findViewById(R.id.imageRecipePreview);
            btnChooseImage = itemView.findViewById(R.id.btnChooseRecipeImage);
        }
    }
}
