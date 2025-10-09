package com.example.final_project.views.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.views.adapter.MenuAdapter;
import com.example.final_project.models.entity.Menu;
import com.example.final_project.models.entity.Recipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MenuAdapter menuAdapter;
    private List<Menu> weeklyMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);

        initViews(view);
        setupRecyclerView();
        loadMockData();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_menu);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        weeklyMenu = new ArrayList<>();
        menuAdapter = new MenuAdapter(weeklyMenu);
        recyclerView.setAdapter(menuAdapter);
    }

    private void loadMockData() {
        // Tạo mock data cho menu tuần
        weeklyMenu.clear();

        // Thứ 2
        List<Recipe> mondayRecipes = Arrays.asList(
            new Recipe("Phở Bò", "Phở bò truyền thống với nước dùng đậm đà", "", "pho_bo", "pho_bo_2", null, null),
            new Recipe("Bún Chả", "Bún chả Hà Nội với thịt nướng thơm ngon", "", "bun_cha", "bun_cha_2", null, null),
            new Recipe("Chè Đậu Xanh", "Chè đậu xanh mát lạnh", "", "che_dau_xanh", "che_dau_xanh_2", null, null)
        );
        weeklyMenu.add(new Menu("Thứ Hai", "Thứ Hai", "Thứ Hai", null, null, null, null));



        android.util.Log.d("RecipeFragment", "Loaded " + weeklyMenu.size() + " days");
        for (Menu dayMenu : weeklyMenu) {
            android.util.Log.d("RecipeFragment", dayMenu.getMenuName() + " has " + dayMenu.getRecipeList().size() + " recipes");
        }

        menuAdapter.notifyDataSetChanged();
    }
}
