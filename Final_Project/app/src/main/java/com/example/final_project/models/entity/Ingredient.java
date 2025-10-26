//package com.example.final_project.models.entity;
//
//public class Ingredient {
//    private String ingredientId;
//    private String amount;
//
//    // Quan hệ N - 1
//    private Recipe recipe;
//    private FoodItem foodItem;
//
//    public Ingredient() {}
//
//    public Ingredient(String ingredientId, String amount, Recipe recipe, FoodItem foodItem) {
//        this.ingredientId = ingredientId;
//        this.amount = amount;
//        this.recipe = recipe;
//        this.foodItem = foodItem;
//    }
//
//    // Getter / Setter
//
//    public String getIngredientId() {
//        return ingredientId;
//    }
//
//    public void setIngredientId(String ingredientId) {
//        this.ingredientId = ingredientId;
//    }
//
//    public String getAmount() {
//        return amount;
//    }
//
//    public void setAmount(String amount) {
//        this.amount = amount;
//    }
//
//    public Recipe getRecipe() {
//        return recipe;
//    }
//
//    public void setRecipe(Recipe recipe) {
//        this.recipe = recipe;
//    }
//
//    public FoodItem getFoodItem() {
//        return foodItem;
//    }
//
//    public void setFoodItem(FoodItem foodItem) {
//        this.foodItem = foodItem;
//    }
//
//}
package com.example.final_project.models.entity;

public class Ingredient {
    private String ingredientId;
    private String amount;

    // Thêm các trường ID để lưu khoá ngoại trực tiếp
    private String recipeId;
    private String foodId;

    // Quan hệ N - 1 (giữ lại để có thể dùng sau này nếu cần)
    private Recipe recipe;
    private FoodItem foodItem;

    public Ingredient() {}

    // Constructor đầy đủ hơn (tùy chọn)
    public Ingredient(String ingredientId, String amount, String recipeId, String foodId) {
        this.ingredientId = ingredientId;
        this.amount = amount;
        this.recipeId = recipeId;
        this.foodId = foodId;
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

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }
}