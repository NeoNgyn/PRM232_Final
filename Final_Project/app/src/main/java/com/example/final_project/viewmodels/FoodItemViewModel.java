package com.example.final_project.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.final_project.models.entity.FoodItem;
import com.example.final_project.models.repository.FoodItemRepository;

import java.util.List;

public class FoodItemViewModel extends ViewModel {
    private final FoodItemRepository repository;

    public FoodItemViewModel() {
        this.repository = new FoodItemRepository();
    }

    public List<FoodItem> getFoodItemsByUserId(String userId) {
        return repository.getFoodItemsByUserId(userId);
    }
}
