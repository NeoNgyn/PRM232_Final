package com.example.final_project.models.entity;

import java.util.Date;
import java.util.List;

public class User {
    private String userId;
    private String fullname;
    private String email;
    private String password;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;

    // Quan hệ 1 - N
    private List<FoodItem> foodItems;
    private List<ScanHistory> scanHistories;

    public User() {}

    public User(String userId, String fullname, String email, String password, boolean isActive, Date createdAt, Date updatedAt) {
        this.userId = userId;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter / Setter

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public List<FoodItem> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    public List<ScanHistory> getScanHistories() {
        return scanHistories;
    }

    public void setScanHistories(List<ScanHistory> scanHistories) {
        this.scanHistories = scanHistories;
    }
}
