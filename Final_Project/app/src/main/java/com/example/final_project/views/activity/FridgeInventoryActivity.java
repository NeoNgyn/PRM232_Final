package com.example.final_project.views.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.final_project.R;
import com.example.final_project.models.entity.FoodItem;
import com.example.final_project.views.adapter.InventoryAdapter;
import com.example.final_project.utils.CloudinaryHelper;
import com.example.final_project.utils.DatabaseConnection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.function.Consumer;

public class FridgeInventoryActivity extends AppCompatActivity implements InventoryAdapter.OnFoodActionListener {

    private RecyclerView recyclerInventory;
    private EditText etSearch;
    private ImageButton btnBack, btnAdd;
    private TextView tvTitle;
    private TextView tvTotalFood, tvNearExpiry;
    private Button btnFilterAll, btnFilterNearExpiry;

    private InventoryAdapter inventoryAdapter;
    private List<FoodItem> foodList;
    private List<FoodItem> filteredFoodList;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final int REQ_PICK_IMAGE = 1234;
    private Uri pendingImageUri;
    private ImageView dialogPreviewImage;

    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge_inventory);

        initViews();
        setupRecyclerView();
        loadFromDatabase();
        setupListeners();
    }

    private void initViews() {
        recyclerInventory = findViewById(R.id.recyclerInventory);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);
        tvTitle = findViewById(R.id.tvTitle);
        tvTotalFood = findViewById(R.id.tvTotalFood);
        tvNearExpiry = findViewById(R.id.tvNearExpiry);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterNearExpiry = findViewById(R.id.btnFilterNearExpiry);
    }

    private void setupRecyclerView() {
        foodList = new ArrayList<>();
        filteredFoodList = new ArrayList<>();
        inventoryAdapter = new InventoryAdapter(this, filteredFoodList, this);
        recyclerInventory.setLayoutManager(new LinearLayoutManager(this));
        recyclerInventory.setAdapter(inventoryAdapter);
    }

    private void updateStats() {
        int total = foodList.size();
        int nearExpiry = 0;
        long currentTime = System.currentTimeMillis();
        long threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L;

        for (FoodItem item : foodList) {
            if (item.getExpiryDate() != null) {
                long expiryTime = item.getExpiryDate().getTime();
                if (expiryTime >= currentTime && expiryTime - currentTime <= threeDaysInMillis) {
                    nearExpiry++;
                }
            }
        }

        tvTotalFood.setText(total + " món");
        tvNearExpiry.setText(nearExpiry + " món");
    }

    private void filterFoodList(String query) {
        filteredFoodList.clear();
        String lowerCaseQuery = query.toLowerCase();
        if (query.isEmpty()) {
            filteredFoodList.addAll(foodList);
        } else {
            for (FoodItem item : foodList) {
                if (item.getFoodName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredFoodList.add(item);
                }
            }
        }
        inventoryAdapter.notifyDataSetChanged();
    }

    private void filterNearExpiry(String query) {
        filteredFoodList.clear();
        long currentTime = System.currentTimeMillis();
        long threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L;

        for (FoodItem food : foodList) {
            boolean matchesSearch = query.isEmpty() || food.getFoodName().toLowerCase().contains(query.toLowerCase());
            boolean isNearExpiry = food.getExpiryDate() != null && (food.getExpiryDate().getTime() >= currentTime && food.getExpiryDate().getTime() - currentTime <= threeDaysInMillis);

            if (matchesSearch && isNearExpiry) {
                filteredFoodList.add(food);
            }
        }
        inventoryAdapter.notifyDataSetChanged();
    }


    private void loadFromDatabase() {
        dbExecutor.execute(() -> {
            List<FoodItem> list = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String sql = "SELECT food_id, food_name, quantity, expiry_date, image_url, create_at, update_at, note FROM FoodItem";
                try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("food_id");
                        String name = rs.getString("food_name");
                        int qty = rs.getInt("quantity");
                        java.sql.Timestamp expirySql = rs.getTimestamp("expiry_date");
                        Date expiry = expirySql == null ? null : new Date(expirySql.getTime());
                        String imageUrl = rs.getString("image_url");
                        String note = rs.getString("note");

                        Date createdAt = null;
                        java.sql.Timestamp tsCreate = rs.getTimestamp("create_at");
                        if (tsCreate != null) createdAt = new Date(tsCreate.getTime());

                        Date updatedAt = null;
                        java.sql.Timestamp tsUpdate = rs.getTimestamp("update_at");
                        if (tsUpdate != null) updatedAt = new Date(tsUpdate.getTime());

                        FoodItem f = new FoodItem(id, name, qty, expiry, imageUrl, createdAt, updatedAt, null, null, null);
                        f.setNote(note);
                        list.add(f);
                    }
                }

                runOnUiThread(() -> {
                    foodList.clear();
                    foodList.addAll(list);
                    filterFoodList(etSearch.getText().toString());
                    updateStats();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(FridgeInventoryActivity.this, "Lỗi load dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void insertFoodToDb(FoodItem food, Runnable onSuccess, Consumer<String> onError) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String sql = "INSERT INTO FoodItem (food_id, food_name, quantity, expiry_date, image_url, note, create_at, update_at) VALUES (?,?,?,?,?,?,?,?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    String id = food.getFoodId();
                    if (id == null || id.isEmpty()) {
                        id = "F" + String.format("%09d", System.currentTimeMillis() % 1000000000L);
                    }
                    if (id.length() > 10) id = id.substring(0, 10);

                    stmt.setString(1, id);
                    stmt.setString(2, food.getFoodName());
                    stmt.setInt(3, food.getQuantity());
                    if (food.getExpiryDate() != null) stmt.setTimestamp(4, new java.sql.Timestamp(food.getExpiryDate().getTime())); else stmt.setNull(4, java.sql.Types.TIMESTAMP);
                    stmt.setString(5, food.getImageUrl());
                    stmt.setString(6, food.getNote());
                    stmt.setTimestamp(7, new java.sql.Timestamp(new Date().getTime()));
                    stmt.setTimestamp(8, new java.sql.Timestamp(new Date().getTime()));

                    if (stmt.executeUpdate() <= 0) throw new SQLException("Insert failed");
                    food.setFoodId(id);
                    runOnUiThread(onSuccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> onError.accept(e.getMessage()));
            }
        });
    }

    private void updateFoodInDb(FoodItem food, Runnable onSuccess, Consumer<String> onError) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String sql = "UPDATE FoodItem SET food_name=?, quantity=?, expiry_date=?, image_url=?, note=?, update_at=? WHERE food_id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, food.getFoodName());
                    stmt.setInt(2, food.getQuantity());
                    if (food.getExpiryDate() != null) stmt.setTimestamp(3, new java.sql.Timestamp(food.getExpiryDate().getTime())); else stmt.setNull(3, java.sql.Types.TIMESTAMP);
                    stmt.setString(4, food.getImageUrl());
                    stmt.setString(5, food.getNote());
                    stmt.setTimestamp(6, new java.sql.Timestamp(new Date().getTime()));
                    stmt.setString(7, food.getFoodId());

                    if (stmt.executeUpdate() <= 0) throw new SQLException("Update failed");
                    runOnUiThread(onSuccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> onError.accept(e.getMessage()));
            }
        });
    }

    private void deleteFoodFromDb(FoodItem food, Runnable onSuccess, Consumer<String> onError) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String sql = "DELETE FROM FoodItem WHERE food_id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, food.getFoodId());
                    if (stmt.executeUpdate() <= 0) throw new SQLException("Delete failed");
                    runOnUiThread(onSuccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> onError.accept(e.getMessage()));
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> showAddOrEditFoodDialog(null));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFoodList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnFilterAll.setOnClickListener(v -> filterFoodList(etSearch.getText().toString()));
        btnFilterNearExpiry.setOnClickListener(v -> filterNearExpiry(etSearch.getText().toString()));
    }

    @Override
    public void onEdit(FoodItem food, int position) {
        showAddOrEditFoodDialog(food);
    }

    @Override
    public void onDelete(FoodItem food, int position) {
        showDeleteConfirmDialog(food);
    }

    private void showAddOrEditFoodDialog(@Nullable FoodItem food) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_food, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvDialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etQty = dialogView.findViewById(R.id.etQty);
        EditText etUnit = dialogView.findViewById(R.id.etUnit);
        EditText etExpiry = dialogView.findViewById(R.id.etExpiry);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        dialogPreviewImage = dialogView.findViewById(R.id.ivPreview);
        Button btnPickImage = dialogView.findViewById(R.id.btnPickImage);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        pendingImageUri = null;
        final boolean isEditing = food != null;

        tvDialogTitle.setText(isEditing ? "Sửa thực phẩm" : "Thêm thực phẩm");
        btnSave.setText(isEditing ? "Cập nhật" : "Lưu");

        if (isEditing) {
            etName.setText(food.getFoodName());
            etQty.setText(String.valueOf(food.getQuantity()));
            if (food.getExpiryDate() != null) etExpiry.setText(dateFormat.format(food.getExpiryDate()));
            etNotes.setText(food.getNote());
            if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
                Glide.with(this).load(food.getImageUrl()).into(dialogPreviewImage);
            }
        }

        btnPickImage.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
            pick.setType("image/*");
            startActivityForResult(Intent.createChooser(pick, "Chọn ảnh"), REQ_PICK_IMAGE);
        });

        etExpiry.setOnClickListener(v -> showDatePicker(etExpiry));

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQty.getText().toString().trim();
            String expiryStr = etExpiry.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (!validateInput(name, qtyStr, expiryStr)) return;

            Runnable dbSuccessAction = () -> {
                loadFromDatabase();
                dialog.dismiss();
                Toast.makeText(this, isEditing ? "Cập nhật thành công!" : "Thêm thành công!", Toast.LENGTH_SHORT).show();
            };

            Consumer<String> dbErrorAction = error -> Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();

            FoodItem itemToSave = isEditing ? food : new FoodItem();
            itemToSave.setFoodName(name);
            itemToSave.setQuantity(Integer.parseInt(qtyStr));
            try {
                if (!expiryStr.isEmpty()) itemToSave.setExpiryDate(dateFormat.parse(expiryStr));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            itemToSave.setNote(notes);

            if (pendingImageUri != null) {
                Toast.makeText(this, "Đang upload ảnh...", Toast.LENGTH_SHORT).show();
                CloudinaryHelper.uploadImage(this, pendingImageUri, new CloudinaryHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String newImageUrl) {
                        itemToSave.setImageUrl(newImageUrl);
                        if (isEditing) updateFoodInDb(itemToSave, dbSuccessAction, dbErrorAction);
                        else insertFoodToDb(itemToSave, dbSuccessAction, dbErrorAction);
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(FridgeInventoryActivity.this, "Lỗi upload ảnh: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (isEditing) {
                    updateFoodInDb(itemToSave, dbSuccessAction, dbErrorAction);
                } else {
                    itemToSave.setImageUrl(""); // Default image if none picked
                    insertFoodToDb(itemToSave, dbSuccessAction, dbErrorAction);
                }
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeleteConfirmDialog(FoodItem food) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa '" + food.getFoodName() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteFoodFromDb(food, () -> {
                        loadFromDatabase();
                        Toast.makeText(FridgeInventoryActivity.this, "Đã xóa thành công!", Toast.LENGTH_SHORT).show();
                    }, error -> {
                        Toast.makeText(FridgeInventoryActivity.this, "Lỗi khi xóa: " + error, Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    editText.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private boolean validateInput(String name, String qtyStr, String expiryStr) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên thực phẩm", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (qtyStr.isEmpty() || Integer.parseInt(qtyStr) <= 0) {
            Toast.makeText(this, "Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (expiryStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập hạn sử dụng", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pendingImageUri = data.getData();
            if (dialogPreviewImage != null) {
                dialogPreviewImage.setImageURI(pendingImageUri);
            }
        }
    }
}
