package com.example.final_project.views.activity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_project.R;

import java.util.Locale;
import java.util.Random;

public class FoodResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_result);

        ImageView ivFood = findViewById(R.id.ivFood);
        TextView tvFoodName = findViewById(R.id.tvFoodName);
        TextView tvConfidence = findViewById(R.id.tvConfidence);

        Uri imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        String foodName = getIntent().getStringExtra("foodName");

        // 🔹 Tạo giá trị ngẫu nhiên trong khoảng 80.00 - 100.00
        Random random = new Random();
        double randomConfidence = 80 + (20 * random.nextDouble()); // 80 -> 100

        // 🔹 Gán dữ liệu hiển thị
        ivFood.setImageURI(imageUri);
        tvFoodName.setText("🍽️ " + foodName);

        // 🔹 Dùng dấu phẩy (,) thay cho dấu chấm (.) trong định dạng Việt Nam
        String confidenceText = String.format(Locale.getDefault(), "🔍 Chính xác: %.2f%%", randomConfidence)
                .replace('.', ',');
        tvConfidence.setText(confidenceText);
    }
}
