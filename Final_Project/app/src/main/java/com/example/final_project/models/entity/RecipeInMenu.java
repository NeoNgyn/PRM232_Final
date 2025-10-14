package com.example.final_project.models.entity;

public class RecipeInMenu {
    private String recipeMenuId;

    // Quan hệ N - 1
    private Recipe recipe;
    private Menu menu;

    public RecipeInMenu() {}

    public RecipeInMenu(String recipeMenuId, Recipe recipe, Menu menu) {
        this.recipeMenuId = recipeMenuId;
        this.recipe = recipe;
        this.menu = menu;
    }

    // Getter / Setter

    public String getRecipeMenuId() {
        return recipeMenuId;
    }

    public void setRecipeMenuId(String recipeMenuId) {
        this.recipeMenuId = recipeMenuId;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }
}
