package com.example.final_project.models.entity;

import java.util.Date;
import java.util.List;

public class Recipe implements java.io.Serializable {
    private String recipeId;
    private String name;
    private String instruction;
    private String nutrition;
    private String imageUrl;
    private Date createdAt;
    private Date updatedAt;

    // Quan hệ 1 - N
    private List<Ingredient> ingredients;

    public Recipe() {}

    public Recipe(String recipeId, String name, String instruction, String nutrition, String imageUrl,
                  Date createdAt, Date updatedAt) {
        this.recipeId = recipeId;
        this.name = name;
        this.instruction = instruction;
        this.nutrition = nutrition;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter / Setter

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getNutrition() {
        return nutrition;
    }

    public void setNutrition(String nutrition) {
        this.nutrition = nutrition;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
}