package com.example.final_project.views.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.models.entity.FoodItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FoodDetailActivity extends AppCompatActivity {

    private ImageView imgFood;
    private TextView tvName, tvExpiry, tvQty, tvNotes;
    private Button btnEdit, btnDelete;
    
    private FoodItem currentFood;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_detail);
        
        initViews();
        loadFoodData();
        setupListeners();
    }

    private void initViews() {
        imgFood = findViewById(R.id.imgFood);
        tvName = findViewById(R.id.tvName);
        tvExpiry = findViewById(R.id.tvExpiry);
        tvQty = findViewById(R.id.tvQty);
        tvNotes = findViewById(R.id.tvNotes);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void loadFoodData() {
        // Trong thực tế, dữ liệu sẽ được truyền qua Intent
        // Ở đây tôi tạo dữ liệu mock để demo
        try {
            currentFood = new FoodItem("1", "Sữa tươi Vinamilk", 2, 
                dateFormat.parse("03/10/2025"), "milk",
                new Date(), new Date(), null, null, null);
            // Set note separately
            currentFood.setNote("Dùng cho bữa sáng");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        updateUI();
    }

    private void updateUI() {
        if (currentFood != null) {
            tvName.setText(currentFood.getFoodName());
            tvExpiry.setText("HSD: " + dateFormat.format(currentFood.getExpiryDate()));
            tvQty.setText("Số lượng: " + currentFood.getQuantity() + " hộp");
            tvNotes.setText("Ghi chú: " + currentFood.getNote());
            
            // Load image
            int resId = getResources().getIdentifier(currentFood.getImageUrl(), "drawable", getPackageName());
            if (resId != 0) {
                imgFood.setImageResource(resId);
            } else {
                imgFood.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> showEditDialog());
        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_food, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etQty = dialogView.findViewById(R.id.etQty);
        EditText etUnit = dialogView.findViewById(R.id.etUnit);
        EditText etExpiry = dialogView.findViewById(R.id.etExpiry);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        // Điền dữ liệu hiện tại
        etName.setText(currentFood.getFoodName());
        etQty.setText(String.valueOf(currentFood.getQuantity()));
        etExpiry.setText(dateFormat.format(currentFood.getExpiryDate()));
        etNotes.setText(currentFood.getNote());
        
        btnSave.setText("✏️ Cập nhật");
        
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQty.getText().toString().trim();
            String unit = etUnit.getText().toString().trim();
            String expiryStr = etExpiry.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();
            
            if (validateInput(name, qtyStr, expiryStr)) {
                try {
                    int quantity = Integer.parseInt(qtyStr);
                    Date expiryDate = dateFormat.parse(expiryStr);
                    
                    currentFood.setFoodName(name);
                    currentFood.setQuantity(quantity);
                    currentFood.setExpiryDate(expiryDate);
                    currentFood.setNote(notes);
                    currentFood.setUpdatedAt(new Date());
                    
                    updateUI();
                    
                    Toast.makeText(this, "Đã cập nhật thực phẩm thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void showDeleteConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);
        
        tvMessage.setText("Bạn có chắc chắn muốn xóa \"" + currentFood.getFoodName() + "\" không? Hành động này không thể hoàn tác.");
        
        btnYes.setOnClickListener(v -> {
            // Trong thực tế, sẽ xóa khỏi database và quay về màn hình trước
            Toast.makeText(this, "Đã xóa thực phẩm thành công!", Toast.LENGTH_SHORT).show();
            finish();
            dialog.dismiss();
        });
        
        btnNo.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private boolean validateInput(String name, String qtyStr, String expiryStr) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên thực phẩm", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (qtyStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                Toast.makeText(this, "Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (expiryStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập hạn sử dụng", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            dateFormat.parse(expiryStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Định dạng ngày không hợp lệ (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
}