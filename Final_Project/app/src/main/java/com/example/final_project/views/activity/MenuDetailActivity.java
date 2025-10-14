package com.example.final_project.views.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.models.entity.RecipeInMenu;
import com.example.final_project.views.adapter.RecipeAdapter;

import java.util.ArrayList;
import java.util.List;

public class MenuDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<RecipeInMenu> recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_detail);

        recyclerView = findViewById(R.id.recipeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Fake data để test
//        recipeList = new ArrayList<>();
//        recipeList.add(new Recipe("Burger Ferguson", "Tasty beef burger with cheese", "", "burger_1", "burger_2", null, null));
//        recipeList.add(new Recipe("Rockin' Burger", "Double beef with spicy sauce", "", "burger2_1", "burger2_2", null, null));
//        recipeList.add(new Recipe("Cheese Lover", "Loaded cheese and tomato", "", "burger3_1", "burger3_2", null, null));

        recipeAdapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(recipeAdapter);
    }
}
