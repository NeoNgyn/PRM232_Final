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

public class MenuListActivity extends AppCompatActivity {

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
        weeklyMenus.get(0).add(new Recipe("Phở Bò", "Phở bò truyền thống với nước dùng thơm đậm đà", "", "pho_bo", "pho_bo_2", null, null));
        weeklyMenus.get(0).add(new Recipe("Bún Chả", "Bún chả Hà Nội với thịt nướng thơm lừng", "", "bun_cha", "bun_cha_2", null, null));
        weeklyMenus.get(0).add(new Recipe("Cơm Tấm", "Cơm tấm sườn nướng với bì chả", "", "com_tam", "com_tam_2", null, null));


    }

    private void setupAdapters() {
        // Thiết lập adapter cho từng ngày trong tuần
        for (int i = 0; i < 7; i++) {
            weeklyAdapters[i] = new WeeklyMenuAdapter(this, weeklyMenus.get(i));
            weeklyRecyclerViews[i].setAdapter(weeklyAdapters[i]);
        }
    }
}
