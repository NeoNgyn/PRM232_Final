//package com.example.final_project;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.material.chip.ChipGroup;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MealDetailActivity extends AppCompatActivity {
//
//    // Lớp nội bộ (inner class) để chứa dữ liệu của một nguyên liệu một cách có cấu trúc
//    private static class Ingredient {
//        String name;
//        String quantity;
//
//        Ingredient(String name, String quantity) {
//            this.name = name;
//            this.quantity = quantity;
//        }
//    }
//
//    private static class Instruction {
//        String stepNumber;
//        String instructionText;
//        String subItemsText;
//
//        Instruction(String stepNumber, String instructionText, String subItemsText) {
//            this.stepNumber = stepNumber;
//            this.instructionText = instructionText;
//            this.subItemsText = subItemsText;
//        }
//    }
//
//    // Biến toàn cục để dễ dàng truy cập container
//    private LinearLayout contentContainer;
//    private LayoutInflater inflater;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_meal_detail);
//
//        // Khởi tạo inflater một lần
//        inflater = LayoutInflater.from(this);
//
//        // --- Phần code cũ để nhận Intent và gán tên món ăn, hình ảnh ---
//        ImageView mealImageView = findViewById(R.id.iv_detail_meal_image);
//        TextView mealNameTextView = findViewById(R.id.tv_detail_meal_name);
//        Intent intent = getIntent();
//        String mealName = intent.getStringExtra("MEAL_NAME");
//        int mealImageResource = intent.getIntExtra("MEAL_IMAGE", R.drawable.placeholder_food);
//        mealNameTextView.setText(mealName);
//        mealImageView.setImageResource(mealImageResource);
//        // ----------------------------------------------------------------
//
//        // Ánh xạ các view chính
//        ChipGroup chipGroup = findViewById(R.id.chip_group);
//        contentContainer = findViewById(R.id.ll_content_container); // THAY ĐỔI: Bây giờ là LinearLayout
//
//        // Thiết lập Listener cho ChipGroup
//        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(@NonNull ChipGroup group, int checkedId) {
//
//                contentContainer.removeAllViews();
//                // Thay vì gán text, chúng ta gọi các hàm hiển thị tương ứng
//                if (checkedId == R.id.chip_ingredients) {
//                    displayIngredients();
//                } else if (checkedId == R.id.chip_cookware) {
//                    displayCookware();
//                } else if (checkedId == R.id.chip_instructions) {
//                    displayInstructions();
//                } else {
//                    contentContainer.removeAllViews(); // Xóa nội dung nếu không có gì được chọn
//                }
//            }
//        });
//
//        // Mặc định chọn Ingredients khi mới mở màn hình
//        chipGroup.check(R.id.chip_ingredients);
//    }
//
//    // Hàm để hiển thị danh sách nguyên liệu
//    private void displayIngredients() {
//        // Chuẩn bị dữ liệu có cấu trúc
//        List<Ingredient> ingredients = new ArrayList<>();
//        ingredients.add(new Ingredient("basmati rice", "½ cup"));
//        ingredients.add(new Ingredient("chicken or vegetable broth", "16 fl oz"));
//        ingredients.add(new Ingredient("cilantro", "½ small bunch"));
//        ingredients.add(new Ingredient("coconut milk", "½ (13.5 fl oz) can"));
//        ingredients.add(new Ingredient("garlic", "1 (1 inch) piece"));
//        ingredients.add(new Ingredient("ginger root", "16 fl oz"));
//        ingredients.add(new Ingredient("grape tomatoes", "1/2 pint"));
//        ingredients.add(new Ingredient("jalapeño pepper", "1"));
//        ingredients.add(new Ingredient("yellow onion", "1 medium"));
//        ingredients.add(new Ingredient("cinnamon, ground", "1 tsp"));
//        ingredients.add(new Ingredient("coriander, ground", "2 tsp"));
//        ingredients.add(new Ingredient("olive oil", "2 tbsp"));
//        ingredients.add(new Ingredient("red lentils", "1 cup"));
//        ingredients.add(new Ingredient("turmeric", "1 tsp"));
//        ingredients.add(new Ingredient("sea salt", "to taste"));
//
//        // Gọi hàm để cập nhật UI
//        updateContentList(ingredients);
//    }
//
//    // Các hàm cho các mục khác (bạn có thể tự điền dữ liệu)
//    private void displayCookware() {
//        // Chuẩn bị dữ liệu cho Cookware, tái sử dụng lớp Ingredient
//        List<Ingredient> cookware = new ArrayList<>();
//        cookware.add(new Ingredient("Large skillet or pot", ""));
//        cookware.add(new Ingredient("Strainer or colander", ""));
//        cookware.add(new Ingredient("Small saucepan", "with lid"));
//        cookware.add(new Ingredient("Spatula or wooden spoon", ""));
//        cookware.add(new Ingredient("Measuring cups and spoons", ""));
//        cookware.add(new Ingredient("Can opener", ""));
//
//        // Gọi hàm để cập nhật UI, tái sử dụng hàm của Ingredients
//        updateContentList(cookware);
//    }
//
//    private void displayInstructions() {
//        List<Instruction> instructions = new ArrayList<>();
//        instructions.add(new Instruction("1", "Using a strainer or colander, rinse the rice under cold, running water, then drain and transfer to a small saucepan. Add broth and bring the mixture to a boil over high heat.", "½ cup basmati rice\n8 fl oz (1 cup) chicken or vegetable broth"));
//        instructions.add(new Instruction("2", "Meanwhile, wash and dry the fresh produce.", "1 (1 inch) piece ginger root\n1 jalapeño pepper\n½ small bunch cilantro\n½ pint grape tomatoes"));
//        instructions.add(new Instruction("3", "Once the liquid comes to a boil, stir the mixture, cover the saucepan, and reduce heat to low. Cook rice until liquid is fully absorbed, 15-18 minutes. Once done, remove rice from the heat and let it stand, still covered.", null));
//        instructions.add(new Instruction("4", "While the rice is cooking, heat olive oil in a large skillet over medium heat. Add diced yellow onion and cook until softened, about 5 minutes.", "1 medium yellow onion\n2 tbsp olive oil"));
//        instructions.add(new Instruction("5", "Add minced garlic, grated ginger, and jalapeño. Cook for another minute until fragrant. Then, add the ground cinnamon, coriander, and turmeric. Stir constantly for 30 seconds.", "1 (1 inch) piece ginger root\n1 jalapeño pepper\ngarlic\ncinnamon, ground\ncoriander, ground\nturmeric"));
//        instructions.add(new Instruction("6", "Stir in the red lentils, grape tomatoes, and coconut milk. Bring to a simmer, then reduce heat to low, cover, and cook for 20-25 minutes, or until lentils are tender.", "1 cup red lentils\n½ pint grape tomatoes\n½ (13.5 fl oz) can coconut milk"));
//        instructions.add(new Instruction("7", "Once lentils are cooked, stir in the cooked basmati rice and chopped cilantro. Season with sea salt to taste. Serve immediately.", "sea salt\ncilantro"));
//
//        // Bắt đầu cập nhật UI
//        for (Instruction item : instructions) {
//            View instructionRow = inflater.inflate(R.layout.item_instruction, contentContainer, false);
//
//            TextView tvStepNumber = instructionRow.findViewById(R.id.tv_step_number);
//            TextView tvInstructionText = instructionRow.findViewById(R.id.tv_instruction_text);
//            TextView tvSubItems = instructionRow.findViewById(R.id.tv_sub_items_text);
//
//            tvStepNumber.setText(item.stepNumber);
//            tvInstructionText.setText(item.instructionText);
//
//            if (item.subItemsText != null && !item.subItemsText.isEmpty()) {
//                tvSubItems.setText(item.subItemsText);
//                tvSubItems.setVisibility(View.VISIBLE);
//            } else {
//                tvSubItems.setVisibility(View.GONE);
//            }
//
//            contentContainer.addView(instructionRow);
//        }
//    }
//
//    // Hàm chính để tạo và thêm các hàng vào LinearLayout
//    private void updateContentList(List<Ingredient> items) {
//        // Xóa hết các view cũ trước khi thêm view mới để tránh trùng lặp
//        contentContainer.removeAllViews();
//
//        for (Ingredient item : items) {
//            // "Thổi phồng" (inflate) file item_ingredient.xml thành một đối tượng View
//            View ingredientRow = inflater.inflate(R.layout.item_ingredient, contentContainer, false);
//
//            // Tìm các TextView bên trong view hàng đó
//            TextView tvName = ingredientRow.findViewById(R.id.tv_ingredient_name);
//            TextView tvQuantity = ingredientRow.findViewById(R.id.tv_ingredient_quantity);
//
//            // Gán dữ liệu từ object vào các TextView
//            tvName.setText(item.name);
//            tvQuantity.setText(item.quantity);
//
//            // Thêm view hàng hoàn chỉnh vào LinearLayout container
//            contentContainer.addView(ingredientRow);
//        }
//    }
//}
package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MealDetailActivity extends AppCompatActivity {

    // =============================================================================================
    // LỚP NỘI BỘ (INNER CLASSES) ĐỂ LƯU TRỮ DỮ LIỆU CÓ CẤU TRÚC
    // =============================================================================================

    private static class Ingredient {
        String name;
        String quantity;

        Ingredient(String name, String quantity) {
            this.name = name;
            this.quantity = quantity;
        }
    }

    private static class Instruction {
        String stepNumber;
        String instructionText;
        String subItemsText; // Nguyên liệu phụ cho bước đó

        Instruction(String stepNumber, String instructionText, String subItemsText) {
            this.stepNumber = stepNumber;
            this.instructionText = instructionText;
            this.subItemsText = subItemsText;
        }
    }

    // =============================================================================================
    // BIẾN TOÀN CỤC
    // =============================================================================================

    private LinearLayout contentContainer;
    private LayoutInflater inflater;

    // =============================================================================================
    // VÒNG ĐỜI ACTIVITY (ACTIVITY LIFECYCLE)
    // =============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        // Khởi tạo các đối tượng cần thiết
        inflater = LayoutInflater.from(this);
        contentContainer = findViewById(R.id.ll_content_container);
        ChipGroup chipGroup = findViewById(R.id.chip_group);

        // Nhận dữ liệu và hiển thị thông tin cơ bản của món ăn
        setupMealInfo();

        // Thiết lập Listener cho ChipGroup để thay đổi nội dung khi người dùng chọn
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, int checkedId) {
                // Xóa nội dung cũ mỗi khi chip thay đổi
                contentContainer.removeAllViews();

                // Gọi hàm hiển thị tương ứng với chip được chọn
                if (checkedId == R.id.chip_ingredients) {
                    displayIngredients();
                } else if (checkedId == R.id.chip_nutrition) { // Đã đổi tên từ chip_cookware
                    displayNutrition();
                } else if (checkedId == R.id.chip_instructions) {
                    displayInstructions();
                }
            }
        });

        // Mặc định chọn "Ingredients" khi màn hình mới được mở
        chipGroup.check(R.id.chip_ingredients);
    }

    // =============================================================================================
    // CÁC HÀM HIỂN THỊ DỮ LIỆU (DATA DISPLAY METHODS)
    // =============================================================================================

    /**
     * Chuẩn bị dữ liệu cho danh sách nguyên liệu và gọi hàm cập nhật UI.
     */
    private void displayIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient("basmati rice", "½ cup"));
        ingredients.add(new Ingredient("chicken or vegetable broth", "16 fl oz"));
        ingredients.add(new Ingredient("cilantro", "½ small bunch"));
        ingredients.add(new Ingredient("coconut milk", "½ (13.5 fl oz) can"));
        ingredients.add(new Ingredient("garlic", "1 (1 inch) piece"));
        ingredients.add(new Ingredient("ginger root", "16 fl oz"));
        ingredients.add(new Ingredient("grape tomatoes", "1/2 pint"));
        ingredients.add(new Ingredient("jalapeño pepper", "1"));
        ingredients.add(new Ingredient("yellow onion", "1 medium"));
        ingredients.add(new Ingredient("cinnamon, ground", "1 tsp"));
        ingredients.add(new Ingredient("coriander, ground", "2 tsp"));
        ingredients.add(new Ingredient("olive oil", "2 tbsp"));
        ingredients.add(new Ingredient("red lentils", "1 cup"));
        ingredients.add(new Ingredient("turmeric", "1 tsp"));
        ingredients.add(new Ingredient("sea salt", "to taste"));

        updateIngredientList(ingredients);
    }

    /**
     * Chuẩn bị dữ liệu cho thông tin dinh dưỡng và gọi hàm cập nhật UI.
     */
    private void displayNutrition() {
        List<Ingredient> nutritionInfo = new ArrayList<>();
        nutritionInfo.add(new Ingredient("Calories", "550 kcal"));
        nutritionInfo.add(new Ingredient("Protein", "25 g"));
        nutritionInfo.add(new Ingredient("Fat", "18 g"));
        nutritionInfo.add(new Ingredient("Carbohydrates", "65 g"));
        nutritionInfo.add(new Ingredient("Sodium", "350 mg"));

        updateIngredientList(nutritionInfo); // Tái sử dụng hàm update của Ingredients
    }

    /**
     * Chuẩn bị dữ liệu cho các bước hướng dẫn và gọi hàm cập nhật UI.
     */
    private void displayInstructions() {
        List<Instruction> instructions = new ArrayList<>();
        instructions.add(new Instruction("1", "Using a strainer or colander, rinse the rice under cold, running water, then drain and transfer to a small saucepan. Add broth and bring the mixture to a boil over high heat.", "½ cup basmati rice\n8 fl oz (1 cup) chicken or vegetable broth"));
        instructions.add(new Instruction("2", "Meanwhile, wash and dry the fresh produce.", "1 (1 inch) piece ginger root\n1 jalapeño pepper\n½ small bunch cilantro\n½ pint grape tomatoes"));
        instructions.add(new Instruction("3", "Once the liquid comes to a boil, stir the mixture, cover the saucepan, and reduce heat to low. Cook rice until liquid is fully absorbed, 15-18 minutes. Once done, remove rice from the heat and let it stand, still covered.", null));
        instructions.add(new Instruction("4", "While the rice is cooking, heat olive oil in a large skillet over medium heat. Add diced yellow onion and cook until softened, about 5 minutes.", "1 medium yellow onion\n2 tbsp olive oil"));
        instructions.add(new Instruction("5", "Add minced garlic, grated ginger, and jalapeño. Cook for another minute until fragrant. Then, add the ground cinnamon, coriander, and turmeric. Stir constantly for 30 seconds.", "1 (1 inch) piece ginger root\n1 jalapeño pepper\ngarlic\ncinnamon, ground\ncoriander, ground\nturmeric"));
        instructions.add(new Instruction("6", "Stir in the red lentils, grape tomatoes, and coconut milk. Bring to a simmer, then reduce heat to low, cover, and cook for 20-25 minutes, or until lentils are tender.", "1 cup red lentils\n½ pint grape tomatoes\n½ (13.5 fl oz) can coconut milk"));
        instructions.add(new Instruction("7", "Once lentils are cooked, stir in the cooked basmati rice and chopped cilantro. Season with sea salt to taste. Serve immediately.", "sea salt\ncilantro"));

        updateInstructionList(instructions);
    }

    // =============================================================================================
    // CÁC HÀM CẬP NHẬT GIAO DIỆN (UI UPDATE METHODS)
    // =============================================================================================

    /**
     * Cập nhật UI cho danh sách nguyên liệu hoặc thông tin dinh dưỡng.
     * @param items Danh sách các đối tượng Ingredient để hiển thị.
     */
    private void updateIngredientList(List<Ingredient> items) {
        for (Ingredient item : items) {
            View row = inflater.inflate(R.layout.item_ingredient, contentContainer, false);

            TextView tvName = row.findViewById(R.id.tv_ingredient_name);
            TextView tvQuantity = row.findViewById(R.id.tv_ingredient_quantity);

            tvName.setText(item.name);
            tvQuantity.setText(item.quantity);

            contentContainer.addView(row);
        }
    }

    /**
     * Cập nhật UI cho danh sách các bước hướng dẫn.
     * @param items Danh sách các đối tượng Instruction để hiển thị.
     */
    private void updateInstructionList(List<Instruction> items) {
        for (Instruction item : items) {
            View row = inflater.inflate(R.layout.item_instruction, contentContainer, false);

            TextView tvStepNumber = row.findViewById(R.id.tv_step_number);
            TextView tvInstructionText = row.findViewById(R.id.tv_instruction_text);
            TextView tvSubItems = row.findViewById(R.id.tv_sub_items_text);

            tvStepNumber.setText(item.stepNumber);
            tvInstructionText.setText(item.instructionText);

            // Chỉ hiển thị phần nguyên liệu phụ nếu có
            if (item.subItemsText != null && !item.subItemsText.isEmpty()) {
                tvSubItems.setText(item.subItemsText);
                tvSubItems.setVisibility(View.VISIBLE);
            } else {
                tvSubItems.setVisibility(View.GONE);
            }

            contentContainer.addView(row);
        }
    }

    /**
     * Lấy dữ liệu từ Intent và thiết lập các thông tin cơ bản cho màn hình.
     */
    private void setupMealInfo() {
        ImageView mealImageView = findViewById(R.id.iv_detail_meal_image);
        TextView mealNameTextView = findViewById(R.id.tv_detail_meal_name);

        Intent intent = getIntent();
        if (intent != null) {
            String mealName = intent.getStringExtra("MEAL_NAME");
            int mealImageResource = intent.getIntExtra("MEAL_IMAGE", R.drawable.placeholder_food);

            mealNameTextView.setText(mealName);
            mealImageView.setImageResource(mealImageResource);
        }
    }
}