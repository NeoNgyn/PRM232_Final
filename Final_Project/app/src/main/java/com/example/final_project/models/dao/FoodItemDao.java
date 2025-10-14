package com.example.final_project.models.dao;

import com.example.final_project.models.entity.FoodItem;
import com.example.final_project.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDao {
    public List<FoodItem> getFoodItemsByUserId(String userId) {
        List<FoodItem> foodItems = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT food_id, food_name, quantity FROM food_item WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                FoodItem item = new FoodItem();
//                item.setFoodId(rs.getString("food_id"));
                item.setFoodName(rs.getString("food_name"));
                item.setQuantity(rs.getInt("quantity"));
                foodItems.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return foodItems;
    }
}
