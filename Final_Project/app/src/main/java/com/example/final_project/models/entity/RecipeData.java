package com.example.final_project.models.entity;

public class RecipeData {private String title;
    private String ingredients; // Nguyên liệu cần thêm
    private String instructions;
    private String time;
    private String nutrition;

    public RecipeData(String title, String ingredients, String instructions, String time, String nutrition) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.time = time;
        this.nutrition = nutrition;
    }

    // Getters
    public String getTitle() { return title; }
    public String getIngredients() { return ingredients; }
    public String getInstructions() { return instructions; }
    public String getTime() { return time; }
    public String getNutrition() { return nutrition; }
}
