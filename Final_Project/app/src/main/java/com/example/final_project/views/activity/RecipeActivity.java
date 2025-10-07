package com.example.final_project.views.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.models.entity.Recipe;
import com.example.final_project.views.adapter.WeeklyMenuAdapter;
import java.util.ArrayList;
import java.util.List;

public class RecipeActivity extends AppCompatActivity {

    private RecyclerView[] weeklyRecyclerViews = new RecyclerView[7];
    private WeeklyMenuAdapter[] weeklyAdapters = new WeeklyMenuAdapter[7];
    private List<List<Recipe>> weeklyMenus = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Khởi tạo RecyclerViews cho 7 ngày
        initRecyclerViews();

        // Khởi tạo dữ liệu thực đơn tuần
        initWeeklyMenuData();

        // Setup adapters cho tất cả các ngày
        setupAdapters();
    }

    private void initRecyclerViews() {
        // Khởi tạo RecyclerView cho từng ngày
        weeklyRecyclerViews[0] = findViewById(R.id.recyclerViewMonday);
        weeklyRecyclerViews[1] = findViewById(R.id.recyclerViewTuesday);
        weeklyRecyclerViews[2] = findViewById(R.id.recyclerViewWednesday);
        weeklyRecyclerViews[3] = findViewById(R.id.recyclerViewThursday);
        weeklyRecyclerViews[4] = findViewById(R.id.recyclerViewFriday);
        weeklyRecyclerViews[5] = findViewById(R.id.recyclerViewSaturday);
        weeklyRecyclerViews[6] = findViewById(R.id.recyclerViewSunday);

        // Thiết lập LinearLayoutManager ngang cho tất cả RecyclerView
        for (int i = 0; i < 7; i++) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            weeklyRecyclerViews[i].setLayoutManager(layoutManager);
            weeklyRecyclerViews[i].setHasFixedSize(true);
        }
    }

    private void initWeeklyMenuData() {
        // Khởi tạo danh sách thực đơn cho 7 ngày
        for (int i = 0; i < 7; i++) {
            weeklyMenus.add(new ArrayList<>());
        }

        // Thứ Hai - 3 món
        weeklyMenus.get(0).add(new Recipe("Phở Bò", "Phở bò truyền thống với nước dùng thơm đậm đà", "", "pho_bo", "pho_bo_2", "pho_bo_3", "Món ăn sáng truyền thống"));
        weeklyMenus.get(0).add(new Recipe("Bún Chả", "Bún chả Hà Nội với thịt nướng thơm lừng", "", "bun_cha", "bun_cha_2", "bun_cha_3", "Đặc sản Hà Nội"));
        weeklyMenus.get(0).add(new Recipe("Cơm Tấm", "Cơm tấm sườn nướng với bì chả", "", "com_tam", "com_tam_2", "com_tam_3", "Món ăn miền Nam"));

        // Thứ Ba - 3 món
        weeklyMenus.get(1).add(new Recipe("Bánh Mì", "Bánh mì Việt Nam giòn rụm", "", "banh_mi", "banh_mi_2", "banh_mi_3", "Top 10 món ăn đường phố"));
        weeklyMenus.get(1).add(new Recipe("Gỏi Cuốn", "Gỏi cuốn tươi mát với tôm thịt", "", "goi_cuon", "goi_cuon_2", "goi_cuon_3", "Món khai vị nhẹ nhàng"));
        weeklyMenus.get(1).add(new Recipe("Bánh Xèo", "Bánh xèo miền Tây giòn tan", "", "banh_xeo", "banh_xeo_2", "banh_xeo_3", "Đặc sản miền Tây"));

        // Thứ Tư - 3 món
        weeklyMenus.get(2).add(new Recipe("Bún Bò Huế", "Bún bò Huế cay nồng đậm đà", "", "bun_bo_hue", "bun_bo_hue_2", "bun_bo_hue_3", "Đặc sản xứ Huế"));
        weeklyMenus.get(2).add(new Recipe("Chả Giò", "Chả giò rán giòn thơm ngon", "", "cha_gio", "cha_gio_2", "cha_gio_3", "Món khai vị truyền thống"));
        weeklyMenus.get(2).add(new Recipe("Hủ Tiếu", "Hủ tiếu Nam Vang ngọt thanh", "", "hu_tieu", "hu_tieu_2", "hu_tieu_3", "Món ăn sáng miền Nam"));

        // Thứ Năm - 3 món
        weeklyMenus.get(3).add(new Recipe("Nem Rán", "Nem rán giòn rụm đậm đà", "", "nem_ran", "nem_ran_2", "nem_ran_3", "Món ăn dịp tết"));
        weeklyMenus.get(3).add(new Recipe("Canh Chua Cá", "Canh chua cá chua nhẹ thanh mát", "", "canh_chua", "canh_chua_2", "canh_chua_3", "Món canh miền Nam"));
        weeklyMenus.get(3).add(new Recipe("Cà Phê Sữa", "Cà phê sữa đá đậm đà", "", "ca_phe_sua", "ca_phe_sua_2", "ca_phe_sua_3", "Thức uống đặc trưng"));

        // Thứ Sáu - 3 món
        weeklyMenus.get(4).add(new Recipe("Mì Quảng", "Mì Quảng đặc sản miền Trung", "", "mi_quang", "mi_quang_2", "mi_quang_3", "Đặc sản Quảng Nam"));
        weeklyMenus.get(4).add(new Recipe("Bánh Cuốn", "Bánh cuốn Thanh Trì mỏng mịn", "", "banh_cuon", "banh_cuon_2", "banh_cuon_3", "Món ăn sáng Hà Nội"));
        weeklyMenus.get(4).add(new Recipe("Thịt Nướng", "Thịt nướng BBQ thơm lừng", "", "thit_nuong", "thit_nuong_2", "thit_nuong_3", "Món nướng phổ biến"));

        // Thứ Bảy - 3 món
        weeklyMenus.get(5).add(new Recipe("Bánh Tráng Nướng", "Bánh tráng nướng Đà Lạt", "", "banh_trang_nuong", "banh_trang_nuong_2", "banh_trang_nuong_3", "Đặc sản Đà Lạt"));
        weeklyMenus.get(5).add(new Recipe("Cháo Lòng", "Cháo lòng nóng hổi bổ dưỡng", "", "chao_long", "chao_long_2", "chao_long_3", "Món ăn sáng bổ dưỡng"));
        weeklyMenus.get(5).add(new Recipe("Bánh Bao", "Bánh bao nhân thịt trứng", "", "banh_bao", "banh_bao_2", "banh_bao_3", "Món ăn sáng tiện lợi"));

        // Chủ Nhật - 3 món
        weeklyMenus.get(6).add(new Recipe("Lẩu Thái", "Lẩu Thái chua cay đặc trưng", "", "lau_thai", "lau_thai_2", "lau_thai_3", "Món ăn gia đình"));
        weeklyMenus.get(6).add(new Recipe("Bánh Chưng", "Bánh chưng truyền thống", "", "banh_chung", "banh_chung_2", "banh_chung_3", "Món ăn ngày tết"));
        weeklyMenus.get(6).add(new Recipe("Chè Ba Màu", "Chè ba màu ngọt mát", "", "che_ba_mau", "che_ba_mau_2", "che_ba_mau_3", "Món tráng miệng"));
    }

    private void setupAdapters() {
        // Thiết lập adapter cho từng ngày trong tuần
        for (int i = 0; i < 7; i++) {
            weeklyAdapters[i] = new WeeklyMenuAdapter(this, weeklyMenus.get(i));
            weeklyRecyclerViews[i].setAdapter(weeklyAdapters[i]);
        }
    }
}
