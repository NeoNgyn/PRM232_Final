package com.example.final_project.views.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.views.adapter.HomeMenuAdapter;
import com.example.final_project.views.adapter.FeaturedFoodAdapter;
import java.util.ArrayList;
import java.util.List;

public class HomeMenuActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMenu;
    private RecyclerView recyclerViewFeatured;
    private HomeMenuAdapter menuAdapter;
    private FeaturedFoodAdapter featuredAdapter;
    private List<HomeMenuAdapter.MenuItem> menuList;
    private List<FeaturedFoodAdapter.FeaturedFood> featuredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_menu);

        // Khởi tạo RecyclerView cho món nổi bật (ngang)
        recyclerViewFeatured = findViewById(R.id.recyclerViewFeatured);
        LinearLayoutManager featuredLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewFeatured.setLayoutManager(featuredLayoutManager);
        recyclerViewFeatured.setHasFixedSize(true);

        // Khởi tạo RecyclerView cho menu (dọc)
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMenu.setHasFixedSize(true);
        recyclerViewMenu.setNestedScrollingEnabled(false);

        // Khởi tạo dữ liệu
        initFeaturedData();
        initMenuData();

        // Khởi tạo và gán adapter
        featuredAdapter = new FeaturedFoodAdapter(this, featuredList);
        recyclerViewFeatured.setAdapter(featuredAdapter);

        menuAdapter = new HomeMenuAdapter(this, menuList);
        recyclerViewMenu.setAdapter(menuAdapter);
    }

    private void initFeaturedData() {
        featuredList = new ArrayList<>();

        // Thêm 4 món ăn nổi bật như trong hình
        featuredList.add(new FeaturedFoodAdapter.FeaturedFood("Bánh chưng", "banh_chung.jpg"));
        featuredList.add(new FeaturedFoodAdapter.FeaturedFood("Bánh mì", "banh_mi.jpg"));
        featuredList.add(new FeaturedFoodAdapter.FeaturedFood("Thịt kho tàu", "thit_kho_tau.jpg"));
        featuredList.add(new FeaturedFoodAdapter.FeaturedFood("Chả cá", "cha_ca.jpg"));
    }

    private void initMenuData() {
        menuList = new ArrayList<>();

        // Thêm dữ liệu mẫu cho menu - tên file ảnh phải khớp với thư mục assets/food_images/
        menuList.add(new HomeMenuAdapter.MenuItem(
            "Bún chả",
            "bun_cha.jpg",
            "Bún chả Hà Nội với thịt nướng thơm ngon, nước mắm chua ngọt."
        ));

        menuList.add(new HomeMenuAdapter.MenuItem(
            "Phở Bò",
            "pho_bo.jpg",
            "Món phở truyền thống Việt Nam với nước dùng thơm ngon, bánh phở mềm và thịt bò tươi"
        ));

        menuList.add(new HomeMenuAdapter.MenuItem(
            "Cơm Tấm",
            "com_tam.jpg",
            "Cơm tấm Sài Gòn truyền thống với sườn nướng, bì, chả, trứng và nước mắm"
        ));

        menuList.add(new HomeMenuAdapter.MenuItem(
            "Bánh Xèo",
            "banh_xeo.jpg",
            "Bánh xèo miền Tây giòn tan với nhân tôm thịt, giá đỗ, ăn kèm rau sống"
        ));

        menuList.add(new HomeMenuAdapter.MenuItem(
            "Gỏi Cuốn",
            "goi_cuon.jpg",
            "Gỏi cuốn tươi mát với tôm thịt, bún, rau sống và nước chấm đậu phộng đặc biệt"
        ));

        menuList.add(new HomeMenuAdapter.MenuItem(
            "Bún Bò Huế",
            "bun_bo_hue.jpg",
            "Bún bò Huế cay nồng với nước dùng đậm đà, thịt bò và chả cua"
        ));
    }
}
