package com.example.final_project.views.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.models.entity.RecipeData;

import java.util.List;

public class RecipeCardAdapter extends RecyclerView.Adapter<RecipeCardAdapter.RecipeCardViewHolder> {

    private List<RecipeData> recipeList;
    private final ChatAdapter.OnRecipeSaveListener saveListener;

    public RecipeCardAdapter(List<RecipeData> recipeList, ChatAdapter.OnRecipeSaveListener saveListener) {
        this.recipeList = recipeList;
        this.saveListener = saveListener;
    }

    // Hàm này để cập nhật dữ liệu khi ViewHolder được tái sử dụng
    public void updateRecipes(List<RecipeData> newRecipeList) {
        this.recipeList = newRecipeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_card, parent, false); // Đây là layout của thẻ món ăn
        return new RecipeCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeCardViewHolder holder, int position) {
        RecipeData recipeData = recipeList.get(position);

        holder.tvTitle.setText(recipeData.getTitle());
        holder.tvIngredients.setText(recipeData.getIngredients());
        holder.tvInstructions.setText(recipeData.getInstructions());
        holder.tvTime.setText("⏱ " + recipeData.getTime());

        // Reset trạng thái nút khi bind để tránh lỗi tái sử dụng
        holder.btnSave.setText("Lưu món ăn");
        holder.btnSave.setEnabled(true);

        holder.btnSave.setOnClickListener(v -> {
            if (saveListener != null) {
                saveListener.onSaveRecipeClicked(recipeData);
                holder.btnSave.setText("✅ Đã lưu");
                holder.btnSave.setEnabled(false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    static class RecipeCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvIngredients;
        TextView tvInstructions;
        TextView tvTime;
        Button btnSave;

        RecipeCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvIngredients = itemView.findViewById(R.id.tvRecipeIngredients);
            tvInstructions = itemView.findViewById(R.id.tvRecipeInstructions);
            tvTime = itemView.findViewById(R.id.tvRecipeTime);
            btnSave = itemView.findViewById(R.id.btnSaveRecipe);
        }
    }
}