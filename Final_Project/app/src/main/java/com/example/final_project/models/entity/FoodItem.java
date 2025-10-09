package com.example.final_project.models.entity;

import java.util.Date;

public class FoodItem {
    private String foodId;
    private String foodName;
    private int quantity;
    private Date expiryDate;
    private String imageUrl;
    private String note;
    private Date createdAt;
    private Date updatedAt;

    // Quan hệ N - 1
    private User user;
    private Category category;
    private Unit unit;

    public FoodItem() {}

    public FoodItem(String foodId, String foodName, int quantity, Date expiryDate, String imageUrl,
                    Date createdAt, Date updatedAt, User user, Category category, Unit unit) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.user = user;
        this.category = category;
        this.unit = unit;
    }

    // Getter / Setter

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}