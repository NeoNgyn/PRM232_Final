package com.example.final_project.views.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.views.adapter.HomeFoodAdapter;
import com.example.final_project.views.adapter.HomeMenuAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private ImageView ivHomeIcon;
    private ImageView ivFridgeIconRight;
    private TextView tvUserName;
    private RecyclerView rvHomeFoods;
    private HomeFoodAdapter homeFoodAdapter;
    private RecyclerView rvHomeMenu;
    private HomeMenuAdapter homeMenuAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupListeners();
        setupHomeFoods(view);
        setupHomeMenu(view);

        return view;
    }

    private void initViews(View view) {
        ivHomeIcon = view.findViewById(R.id.ivHomeIcon);
        ivFridgeIconRight = view.findViewById(R.id.ivFridgeIconRight);
        tvUserName = view.findViewById(R.id.tvUserName);

        // Set tên người dùng mặc định
        tvUserName.setText("Xin chào, Admin");
    }

    private void setupListeners() {
        ivHomeIcon.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Trang chủ", Toast.LENGTH_SHORT).show();
        });

        ivFridgeIconRight.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tủ lạnh", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupHomeFoods(View view) {
        rvHomeFoods = view.findViewById(R.id.rvHomeFoods);
        rvHomeFoods.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        List<HomeFoodAdapter.FoodItem> foodList = getRandomFoods();
        homeFoodAdapter = new HomeFoodAdapter(getContext(), foodList);
        rvHomeFoods.setAdapter(homeFoodAdapter);
    }

    private void setupHomeMenu(View view) {
        rvHomeMenu = view.findViewById(R.id.rvHomeMenu);
        rvHomeMenu.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        List<HomeMenuAdapter.MenuItem> menuList = getMenuItems();
        homeMenuAdapter = new HomeMenuAdapter(getContext(), menuList);
        rvHomeMenu.setAdapter(homeMenuAdapter);
    }

    private List<HomeFoodAdapter.FoodItem> getRandomFoods() {
        // Danh sách mock tên món ăn và tên file ảnh (không có đuôi)
        List<HomeFoodAdapter.FoodItem> allFoods = new ArrayList<>();
        allFoods.add(new HomeFoodAdapter.FoodItem("Phở bò", "pho_bo.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Bún chả", "bun_cha.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Cơm tấm", "com_tam.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Bánh mì", "banh_mi.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Bún bò Huế", "bun_bo_hue.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Nem nướng", "nem_nuong.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Mì Quảng", "mi_quang.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Bánh xèo", "banh_xeo.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Hủ tiếu", "hu_tieu.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Bánh cuốn", "banh_cuon.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Chả cá", "cha_ca.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Bánh chưng", "banh_chung.jpg"));
        allFoods.add(new HomeFoodAdapter.FoodItem("Thịt kho tàu", "thit_kho_tau.jpg"));
        // Trộn ngẫu nhiên và lấy 6 món
        Collections.shuffle(allFoods);
        return allFoods.subList(0, Math.min(6, allFoods.size()));
    }

    private List<HomeMenuAdapter.MenuItem> getMenuItems() {
        List<HomeMenuAdapter.MenuItem> menuList = new ArrayList<>();
        menuList.add(new HomeMenuAdapter.MenuItem("Phở bò", "pho_bo.jpg", "Phở bò Hà Nội với nước dùng đậm đà, thịt bò mềm thơm."));
        menuList.add(new HomeMenuAdapter.MenuItem("Bún chả", "bun_cha.jpg", "Bún chả Hà Nội với thịt nướng thơm ngon, nước mắm chua ngọt."));
        menuList.add(new HomeMenuAdapter.MenuItem("Cơm tấm", "com_tam.jpg", "Cơm tấm Sài Gòn ăn kèm sườn nướng, bì, chả trứng."));
        menuList.add(new HomeMenuAdapter.MenuItem("Bánh mì", "banh_mi.jpg", "Bánh mì Việt Nam giòn rụm, nhân đa dạng, ăn sáng tiện lợi."));
        menuList.add(new HomeMenuAdapter.MenuItem("Bún bò Huế", "bun_bo_hue.jpg", "Bún bò Huế cay nồng, nước dùng đậm vị, thịt bò và giò heo."));
        menuList.add(new HomeMenuAdapter.MenuItem("Nem nướng", "nem_nuong.jpg", "Nem nướng Nha Trang thơm lừng, ăn kèm rau sống và nước chấm."));
        menuList.add(new HomeMenuAdapter.MenuItem("Mì Quảng", "mi_quang.jpg", "Mì Quảng Đà Nẵng với tôm, thịt, trứng, nước dùng đặc trưng."));
        menuList.add(new HomeMenuAdapter.MenuItem("Bánh xèo", "banh_xeo.jpg", "Bánh xèo vàng giòn, nhân tôm thịt, ăn kèm rau sống."));
        menuList.add(new HomeMenuAdapter.MenuItem("Hủ tiếu", "hu_tieu.jpg", "Hủ tiếu Nam Vang với nước dùng ngọt thanh, topping đa dạng."));
        menuList.add(new HomeMenuAdapter.MenuItem("Bánh cuốn", "banh_cuon.jpg", "Bánh cuốn mềm mịn, nhân thịt, ăn kèm chả và nước mắm."));
        menuList.add(new HomeMenuAdapter.MenuItem("Chả cá", "cha_ca.jpg", "Chả cá Lã Vọng Hà Nội, cá nướng thơm, ăn kèm bún và rau."));
        menuList.add(new HomeMenuAdapter.MenuItem("Bánh chưng", "banh_chung.jpg", "Bánh chưng truyền thống ngày Tết, nhân thịt mỡ đậu xanh."));
        menuList.add(new HomeMenuAdapter.MenuItem("Thịt kho tàu", "thit_kho_tau.jpg", "Thịt kho tàu miền Nam, thịt ba chỉ kho trứng vị đậm đà."));
        return menuList;
    }
}
