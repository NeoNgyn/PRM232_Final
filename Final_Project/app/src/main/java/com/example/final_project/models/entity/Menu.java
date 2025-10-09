package com.example.final_project.models.entity;

import java.util.Date;
import java.util.List;

public class Menu {
    private String menuId;
    private String menuName;
    private String description;
    private Date fromDate;
    private Date toDate;
    private Date createdAt;
    private Date updatedAt;

    // Quan hệ 1 - N
    private List<RecipeInMenu> recipeList;

    public Menu() {}

    public Menu(String menuId, String menuName, String description, Date fromDate, Date toDate,
                Date createdAt, Date updatedAt) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.description = description;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter / Setter

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
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

    public List<RecipeInMenu> getRecipeList() {
        return recipeList;
    }

    public void setRecipeList(List<RecipeInMenu> recipeList) {
        this.recipeList = recipeList;
    }
}
