package com.example.final_project.models.repository;

import com.example.final_project.models.dao.FoodItemDao;
import com.example.final_project.models.entity.FoodItem;

import java.util.List;

public class FoodItemRepository {
    private final FoodItemDao foodItemDao;

    public FoodItemRepository() {
        this.foodItemDao = new FoodItemDao();
    }

    public List<FoodItem> getFoodItemsByUserId(String userId) {
        return foodItemDao.getFoodItemsByUserId(userId);
    }
}
