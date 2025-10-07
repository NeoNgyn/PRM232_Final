package com.example.assignment_task1.utils;

import android.content.Context;
import android.util.Log;
import com.example.assignment_task1.R;
import java.io.IOException;

public class ImageHelper {
    private static final String TAG = "ImageHelper";

    public static int getRecipeImage(String recipeName) {
        if (recipeName == null) {
            return R.drawable.ic_food_placeholder;
        }

        String name = recipeName.toLowerCase();

        // Phở và các món bún
        if (name.contains("phở") || name.contains("pho")) {
            return R.drawable.ic_pho;
        } else if (name.contains("bún") || name.contains("bun") || name.contains("hủ tiếu")) {
            return R.drawable.ic_pho;
        } else if (name.contains("cơm") || name.contains("com")) {
            return R.drawable.ic_rice;
        } else if (name.contains("bánh") || name.contains("banh")) {
            return R.drawable.ic_bread;
        } else if (name.contains("chè") || name.contains("che") ||
                   name.contains("kem") || name.contains("dessert")) {
            return R.drawable.ic_dessert;
        } else if (name.contains("sinh tố") || name.contains("nước") ||
                   name.contains("trà") || name.contains("cà phê") ||
                   name.contains("juice") || name.contains("tea") ||
                   name.contains("coffee")) {
            return R.drawable.ic_drink;
        } else if (name.contains("thịt") || name.contains("nem") ||
                   name.contains("chả") || name.contains("meat")) {
            return R.drawable.ic_food_placeholder;
        } else {
            return R.drawable.ic_food_placeholder;
        }
    }

    // Phương thức để thêm ảnh từ URL (sử dụng Glide hoặc Picasso sau này)
    public static String getRecipeImageUrl(String recipeName) {
        // Trả về URL ảnh từ server hoặc local assets
        return ""; // Placeholder cho URL
    }

    // Phương thức để load ảnh từ assets folder
    public static String getAssetImagePath(String recipeName) {
        if (recipeName == null) return "food_images/default.png";
        
        String name = recipeName.toLowerCase().trim();
        
        // Map cụ thể cho các món ăn có ảnh thật
        if (name.equals("phở bò")) {
            return "food_images/pho_bo.jpg";
        } else if (name.equals("bún chả")) {
            return "food_images/bun_cha.jpg";
        } else if (name.equals("cơm tấm")) {
            return "food_images/com_tam.jpg";
        } else if (name.equals("bánh mì")) {
            return "food_images/banh_mi.jpg";
        } else if (name.equals("chè đậu xanh")) {
            return "food_images/che_dau_xanh.jpg";
        } else if (name.equals("sinh tố") || name.equals("sinh tố bơ sáp")) {
            return "food_images/sinh_to.jpg";
        } else if (name.equals("bún bò huế")) {
            return "food_images/bun_bo_hue.jpg";
        } else if (name.equals("nem nướng")) {
            return "food_images/nem_nuong.jpg";
        } else if (name.equals("trà sữa")) {
            return "food_images/tra_sua.jpg";
        } else if (name.equals("mì quảng")) {
            return "food_images/mi_quang.jpg";
        } else if (name.equals("bánh xèo")) {
            return "food_images/banh_xeo.jpg";
        } else if (name.equals("nước mía")) {
            return "food_images/nuoc_mia.jpg"; // Note the webp extension
        } else if (name.equals("hủ tiếu")) {
            return "food_images/hu_tieu.jpg";
        } else if (name.equals("cà phê")) {
            return "food_images/ca_phe.jpg";
        } else if (name.equals("chả cá")) {
            return "food_images/cha_ca.jpg";
        } else if (name.equals("kem")) {
            return "food_images/kem.png"; // Note the png extension
        } else if (name.equals("thịt kho tàu")) {
            return "food_images/thit_kho_tau.jpg";
        } else if (name.equals("chè cung đình")) {
            return "food_images/che_cung_dinh.jpg";
        } else {
            // Fallback: thử convert tên món ăn thành tên file
            String fileName = name.toLowerCase()
                    .replaceAll("\\s+", "_")
                    .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                    .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                    .replaceAll("[ìíịỉĩ]", "i")
                    .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                    .replaceAll("[ùúụủũưừứựửữ]", "u")
                    .replaceAll("[ỳýỵỷỹ]", "y")
                    .replaceAll("[đ]", "d")
                    .replaceAll("[^a-z0-9_]", "_");

            // Try different file extensions
            return findExistingImageFile(fileName);
        }
    }

    // New method to try different file extensions
    private static String findExistingImageFile(String baseFileName) {
        Log.d(TAG, "Looking for image with base name: " + baseFileName);
        return "food_images/" + baseFileName + ".jpg";
    }

    // Method to check if file exists in assets (to be used from Activity/Fragment)
    public static boolean fileExistsInAssets(Context context, String fileName) {
        try {
            context.getAssets().open(fileName);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // New method that attempts to find the correct image with multiple extensions
    public static String getBestMatchingImagePath(Context context, String recipeName) {
        String basePath = getAssetImagePath(recipeName);

        // If we're returning a direct mapping, trust it
        if (!basePath.equals("food_images/default.png") &&
            fileExistsInAssets(context, basePath)) {
            return basePath;
        }

        // Extract the base file name without extension
        String fileName = basePath.substring("food_images/".length(), basePath.lastIndexOf("."));

        // Try different extensions
        String[] extensions = {".jpg", ".jpeg", ".png", ".webp"};
        for (String ext : extensions) {
            String testPath = "food_images/" + fileName + ext;
            if (fileExistsInAssets(context, testPath)) {
                Log.d(TAG, "Found matching image: " + testPath);
                return testPath;
            }
        }

        // If nothing found, return default
        return "food_images/default.png";
    }
}
