package com.example.final_project.model;

import java.util.List;

public class DayMenu {
    private String dayName;
    private List<Recipe> recipes;

    public DayMenu(String dayName, List<Recipe> recipes) {
        this.dayName = dayName;
        this.recipes = recipes;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }
}
