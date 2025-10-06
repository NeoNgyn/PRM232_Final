package com.example.final_project.views.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.models.entity.Meal;
import com.example.final_project.views.adapter.MealAdapter;

import java.util.ArrayList;
import java.util.List;

public class MealPlanActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_meal_plan);

        // 1. Tìm các RecyclerView trong layout
        RecyclerView rvMostPopular = findViewById(R.id.rv_most_popular);
        RecyclerView rvRecentlyCreated = findViewById(R.id.rv_recently_created);
        RecyclerView rvRecommended = findViewById(R.id.rv_recommended);

        // 2. Tạo dữ liệu mẫu
        List<Meal> mealList = createSampleMeals();

        // 3. Tạo Adapter
        MealAdapter adapter = new MealAdapter(mealList);

        // 4. Thiết lập cho từng RecyclerView
        setupRecyclerView(rvMostPopular, adapter);
        setupRecyclerView(rvRecentlyCreated, adapter);
        setupRecyclerView(rvRecommended, adapter);
    }

    // Hàm thiết lập chung cho một RecyclerView
    private void setupRecyclerView(RecyclerView recyclerView, MealAdapter adapter) {
        // Đặt LayoutManager để cuộn ngang
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Gán Adapter
        recyclerView.setAdapter(adapter);
    }

    // Hàm tạo danh sách món ăn mẫu
    private List<Meal> createSampleMeals() {
        List<Meal> meals = new ArrayList<>();
        // Bạn cần có các ảnh mẫu trong thư mục res/drawable
        // Ví dụ: R.drawable.meal1, R.drawable.meal2,...
        meals.add(new Meal("Brussels Sprouts Bowl", R.drawable.placeholder_food, true));
        meals.add(new Meal("Roasted Cauliflower Bowl", R.drawable.placeholder_food, false));
        meals.add(new Meal("Creamy Cashew Noodles", R.drawable.placeholder_food, false));
        meals.add(new Meal("Indian Butter Chicken", R.drawable.placeholder_food, false));
        meals.add(new Meal("Greek Salad with Feta", R.drawable.placeholder_food, true));
        return meals;
    }
}