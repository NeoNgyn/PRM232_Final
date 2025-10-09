package com.example.final_project.models.entity;

public class Ingredient {
    private String ingredientId;
    private String amount;

    // Quan hệ N - 1
    private Recipe recipe;
    private FoodItem foodItem;

    public Ingredient() {}

    public Ingredient(String ingredientId, String amount, Recipe recipe, FoodItem foodItem) {
        this.ingredientId = ingredientId;
        this.amount = amount;
        this.recipe = recipe;
        this.foodItem = foodItem;
    }

    // Getter / Setter

    public String getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(String ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public FoodItem getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
    }
}