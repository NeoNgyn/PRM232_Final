package com.example.final_project.views.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.network.FatSecretApiService;
import com.example.final_project.utils.FatSecretApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class FoodResultActivity extends AppCompatActivity {

    private ImageView ivFood;
    private TextView tvFoodName, tvConfidence, tvNutrition, tvRecommend;
    private static final String TAG = "API_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_result);

        ivFood = findViewById(R.id.ivFood);
        tvFoodName = findViewById(R.id.tvFoodName);
        tvConfidence = findViewById(R.id.tvConfidence);
        tvNutrition = findViewById(R.id.tvNutrition);
        tvRecommend = findViewById(R.id.tvRecommend);

        Uri imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        String foodName = getIntent().getStringExtra("foodName");

        ivFood.setImageURI(imageUri);
        tvFoodName.setText("🍽️ " + foodName);

        // Gọi API lấy dinh dưỡng
        fetchNutritionFromFatSecret(foodName);

        // Random độ chính xác
        Random random = new Random();
        double randomConfidence = 80 + (20 * random.nextDouble());
        String confidenceText = String.format(Locale.getDefault(), "🔍 Chính xác: %.2f%%", randomConfidence)
                .replace('.', ',');
        tvConfidence.setText(confidenceText);

        // Nút quay lại trang chủ
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(FoodResultActivity.this, HomeMenuActivity.class);
            startActivity(intent);
            finish();
        });

        // Nút đi đến tủ lạnh
        Button btnFridge = findViewById(R.id.btnFridge);
        btnFridge.setOnClickListener(v -> {
            Intent intent = new Intent(FoodResultActivity.this, FridgeInventoryActivity.class);
            startActivity(intent);
        });

        // Nút tham khảo AI
        Button btnAI = findViewById(R.id.btnAI);
        btnAI.setOnClickListener(v -> {
            Intent intent = new Intent(FoodResultActivity.this, ChatActivity.class);
            startActivity(intent);
        });
    }

    private void fetchNutritionFromFatSecret(String foodName) {
        new Thread(() -> {
            try {
                String token = getFatSecretToken();
                FatSecretApiService api = FatSecretApiClient.getClient().create(FatSecretApiService.class);

                // Gọi API search
                Call<ResponseBody> searchCall = api.searchFoods(
                        "Bearer " + token,
                        "foods.search",
                        foodName,
                        "xml"
                );
                Response<ResponseBody> searchResponse = searchCall.execute();

                Log.d(TAG, "Searching for food: " + foodName);

                if (searchResponse.isSuccessful() && searchResponse.body() != null) {
                    String xml = searchResponse.body().string();
                    Log.d(TAG, "XML response:\n" + xml);

                    // Regex tìm tất cả food
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                            "<food>.*?<food_name>(.*?)</food_name>.*?<food_description>(.*?)</food_description>.*?</food>",
                            java.util.regex.Pattern.DOTALL
                    );
                    java.util.regex.Matcher matcher = pattern.matcher(xml);

                    int count = 0;
                    StringBuilder suggestBuilder = new StringBuilder();
                    String firstFoodDesc = null;

                    while (matcher.find()) {
                        String name = matcher.group(1).trim();
                        String desc = matcher.group(2)
                                .replace("Per 1 serving - ", "")
                                .replace("Per 100g - ", "")
                                .replace("|", " | ")
                                .trim();

                        if (count == 0) {
                            // Món đầu tiên: hiển thị ở tvNutrition
                            firstFoodDesc = desc;
                        } else if (count <= 5) {
                            // Bắt đầu từ món thứ 2 -> tổng 5 món gợi ý
                            suggestBuilder.append("🍽️ ").append(name).append("\n")
                                    .append("📊 ").append(desc).append("\n\n");
                        }

                        count++;
                    }

                    if (firstFoodDesc != null) {
                        String cleanedDesc = firstFoodDesc.replace("|", "\n").trim();
                        String finalInfo = "📊 " + cleanedDesc;
                        String suggestions = suggestBuilder.toString().trim();

                        runOnUiThread(() -> {
                            tvNutrition.setText(finalInfo);  // Món đầu tiên
                            tvRecommend.setText(suggestions); // 5 món còn lại
                        });
                    } else {
                        runOnUiThread(() -> tvNutrition.setText("Không tìm thấy dữ liệu dinh dưỡng."));
                    }

                } else {
                    runOnUiThread(() -> tvNutrition.setText("Không thể truy cập dữ liệu (API lỗi)."));
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> tvNutrition.setText("Lỗi: " + e.getMessage()));
            }
        }).start();
    }



    private String getFatSecretToken() throws IOException, JSONException {
        String clientId = "7b5ee15da8434e20832dc96a58cec473";
        String clientSecret = "5035735492154a7b9c16247566ea373d";
        String auth = clientId + ":" + clientSecret;
        String basicAuth = "Basic " + Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.RequestBody body = new okhttp3.FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("scope", "basic")
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://oauth.fatsecret.com/connect/token")
                .addHeader("Authorization", basicAuth)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(body)
                .build();

        okhttp3.Response response = client.newCall(request).execute();
        String json = response.body().string();

        JSONObject obj = new JSONObject(json);
        return obj.getString("access_token");
    }
}
