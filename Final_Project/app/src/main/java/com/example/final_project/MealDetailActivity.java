package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MealDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        // 1. Ánh xạ (tìm) các View từ file layout XML
        ImageView mealImageView = findViewById(R.id.iv_detail_meal_image);
        TextView mealNameTextView = findViewById(R.id.tv_detail_meal_name);
        TextView mealInfoTextView = findViewById(R.id.tv_detail_info);

        // 2. Lấy Intent đã khởi chạy Activity này
        Intent intent = getIntent();

        // 3. Nhận dữ liệu đã được gửi qua từ Adapter
        String mealName = intent.getStringExtra("MEAL_NAME");
        int mealImageResource = intent.getIntExtra("MEAL_IMAGE", R.drawable.placeholder_food); // Dùng placeholder_food làm ảnh mặc định nếu có lỗi

        // 4. Gán dữ liệu nhận được lên các View tương ứng
        mealNameTextView.setText(mealName);
        mealImageView.setImageResource(mealImageResource);
        // Bạn cũng có thể gán các thông tin khác nếu có, ví dụ:
        // mealInfoTextView.setText("35 minutes • 2 servings");
    }
}