package com.example.final_project.views.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.models.entity.Menu;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<Menu> dayMenus;

    public MenuAdapter(List<Menu> dayMenus) {
        this.dayMenus = dayMenus;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_day, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Menu dayMenu = dayMenus.get(position);
        holder.bind(dayMenu);
    }

    @Override
    public int getItemCount() {
        return dayMenus.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        private TextView textDayName;
        private RecyclerView recyclerViewRecipes;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            textDayName = itemView.findViewById(R.id.text_day_name);
            recyclerViewRecipes = itemView.findViewById(R.id.recycler_recipes);

            // Setup RecyclerView for recipes với orientation horizontal
            LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
            recyclerViewRecipes.setLayoutManager(layoutManager);
        }

        public void bind(Menu dayMenu) {
            textDayName.setText(dayMenu.getMenuName());

            RecipeAdapter recipeAdapter = new RecipeAdapter(dayMenu.getRecipeList());
            recyclerViewRecipes.setAdapter(recipeAdapter);
        }
    }
}
