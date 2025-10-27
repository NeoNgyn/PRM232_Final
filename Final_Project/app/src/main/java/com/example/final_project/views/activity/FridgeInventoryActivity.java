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
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.final_project.R;
import com.example.final_project.models.entity.FoodItem;
import com.example.final_project.views.adapter.InventoryAdapter;
import com.example.final_project.utils.CloudinaryHelper;
import com.example.final_project.utils.DatabaseConnection;
import com.example.final_project.utils.UserSessionManager;

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
import java.util.HashMap;
import java.util.Map;

public class FridgeInventoryActivity extends AppCompatActivity implements InventoryAdapter.OnFoodActionListener {

    private RecyclerView recyclerInventory;
    private EditText etSearch;
    private ImageButton btnBack, btnAdd;
    private TextView tvTitle;
    private TextView tvTotalFood, tvNearExpiry;
    private Button btnFilterAll, btnFilterNearExpiry;
    private com.google.android.material.button.MaterialButton btnMenuNav;
    private com.google.android.material.button.MaterialButton btnFridgeNav;
    // private com.google.android.material.button.MaterialButton btnLogout; // Commented out - moved to Home Menu Activity Avatar Menu
    private CardView headerCardView;

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
        setupNavigationButtons();
    }

    private void initViews() {
        headerCardView = findViewById(R.id.headerCardView);
        recyclerInventory = findViewById(R.id.recyclerInventory);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);
        tvTitle = findViewById(R.id.tvTitle);
        tvTotalFood = findViewById(R.id.tvTotalFood);
        tvNearExpiry = findViewById(R.id.tvNearExpiry);
        btnMenuNav = findViewById(R.id.btnMenuNav);
        btnFridgeNav = findViewById(R.id.btnFridgeNav);
        // btnLogout = findViewById(R.id.btnLogout); // Commented out - moved to Home Menu Activity Avatar Menu
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterNearExpiry = findViewById(R.id.btnFilterNearExpiry);

        // Note: Removed bringToFront() as it causes header to appear at bottom in LinearLayout
        // The header elevation in XML (8dp) is sufficient to keep it above other views
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
            try {
                // Check if user is logged in first
                String currentUserId = UserSessionManager.getInstance(this).getCurrentUserId();
                if (currentUserId == null || currentUserId.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(FridgeInventoryActivity.this, "User not logged in. Please login again.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(FridgeInventoryActivity.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                    return;
                }

                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) throw new SQLException("DB connection is null");

                    String sql = "SELECT f.food_id, f.food_name, f.quantity, f.expiry_date, f.image_url, f.create_at, f.update_at, " +
                                 "c.category_id, c.category_name, u.unit_id, u.unit_name " +
                                 "FROM FoodItem f " +
                                 "LEFT JOIN Category c ON f.category_id = c.category_id " +
                                 "LEFT JOIN Unit u ON f.unit_id = u.unit_id " +
                                 "WHERE f.user_id = ? ORDER BY f.create_at DESC";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, currentUserId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                String id = rs.getString("food_id");
                                String name = rs.getString("food_name");
                                int qty = rs.getInt("quantity");
                                java.sql.Timestamp expirySql = rs.getTimestamp("expiry_date");
                                Date expiry = expirySql == null ? null : new Date(expirySql.getTime());
                                String imageUrl = rs.getString("image_url");

                                Date createdAt = null;
                                java.sql.Timestamp tsCreate = rs.getTimestamp("create_at");
                                if (tsCreate != null) createdAt = new Date(tsCreate.getTime());

                                Date updatedAt = null;
                                java.sql.Timestamp tsUpdate = rs.getTimestamp("update_at");
                                if (tsUpdate != null) updatedAt = new Date(tsUpdate.getTime());

                                // Build Category object
                                com.example.final_project.models.entity.Category category = null;
                                int categoryId = rs.getInt("category_id");
                                if (categoryId > 0) {
                                    String categoryName = rs.getString("category_name");
                                    category = new com.example.final_project.models.entity.Category(categoryId, categoryName);
                                }
                                
                                // Build Unit object
                                com.example.final_project.models.entity.Unit unit = null;
                                int unitId = rs.getInt("unit_id");
                                if (unitId > 0) {
                                    String unitName = rs.getString("unit_name");
                                    unit = new com.example.final_project.models.entity.Unit(unitId, unitName);
                                }

                                FoodItem f = new FoodItem(id, name, qty, expiry, imageUrl, createdAt, updatedAt, null, category, unit);
                                list.add(f);
                            }
                        }
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
                runOnUiThread(() -> Toast.makeText(FridgeInventoryActivity.this, "Error loading food items: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void insertFoodToDb(FoodItem food, Runnable onSuccess, Consumer<String> onError, String categoryName, String unitName) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String currentUserId = UserSessionManager.getInstance(this).getRequiredUserId();
                
                // Get category_id and unit_id from names
                String getCategoryIdSql = "SELECT category_id FROM category WHERE category_name = ?";
                String categoryId = null;
                try (PreparedStatement stmt = conn.prepareStatement(getCategoryIdSql)) {
                    stmt.setString(1, categoryName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) categoryId = rs.getString("category_id");
                    }
                }
                
                String getUnitIdSql = "SELECT unit_id FROM unit WHERE unit_name = ?";
                String unitId = null;
                try (PreparedStatement stmt = conn.prepareStatement(getUnitIdSql)) {
                    stmt.setString(1, unitName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) unitId = rs.getString("unit_id");
                    }
                }
                
                if (categoryId == null || unitId == null) throw new SQLException("Invalid category or unit");
                
                String sql = "INSERT INTO FoodItem (food_id, food_name, quantity, expiry_date, image_url, category_id, unit_id, create_at, update_at, user_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
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
                    stmt.setString(6, categoryId);
                    stmt.setString(7, unitId);
                    stmt.setTimestamp(8, new java.sql.Timestamp(new Date().getTime()));
                    stmt.setTimestamp(9, new java.sql.Timestamp(new Date().getTime()));
                    stmt.setString(10, currentUserId);

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

    private void updateFoodInDb(FoodItem food, Runnable onSuccess, Consumer<String> onError, String categoryName, String unitName) {
        dbExecutor.execute(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) throw new SQLException("DB connection is null");

                String currentUserId = UserSessionManager.getInstance(this).getRequiredUserId();
                
                // Get category_id and unit_id from names
                String getCategoryIdSql = "SELECT category_id FROM category WHERE category_name = ?";
                String categoryId = null;
                try (PreparedStatement stmt = conn.prepareStatement(getCategoryIdSql)) {
                    stmt.setString(1, categoryName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) categoryId = rs.getString("category_id");
                    }
                }
                
                String getUnitIdSql = "SELECT unit_id FROM unit WHERE unit_name = ?";
                String unitId = null;
                try (PreparedStatement stmt = conn.prepareStatement(getUnitIdSql)) {
                    stmt.setString(1, unitName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) unitId = rs.getString("unit_id");
                    }
                }
                
                if (categoryId == null || unitId == null) throw new SQLException("Invalid category or unit");
                
                String sql = "UPDATE FoodItem SET food_name=?, quantity=?, expiry_date=?, image_url=?, category_id=?, unit_id=?, update_at=? WHERE food_id=? AND user_id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, food.getFoodName());
                    stmt.setInt(2, food.getQuantity());
                    if (food.getExpiryDate() != null) stmt.setTimestamp(3, new java.sql.Timestamp(food.getExpiryDate().getTime())); else stmt.setNull(3, java.sql.Types.TIMESTAMP);
                    stmt.setString(4, food.getImageUrl());
                    stmt.setString(5, categoryId);
                    stmt.setString(6, unitId);
                    stmt.setTimestamp(7, new java.sql.Timestamp(new Date().getTime()));
                    stmt.setString(8, food.getFoodId());
                    stmt.setString(9, currentUserId);

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

                String currentUserId = UserSessionManager.getInstance(this).getRequiredUserId();
                String sql = "DELETE FROM FoodItem WHERE food_id=? AND user_id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, food.getFoodId());
                    stmt.setString(2, currentUserId);
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

    private void setupNavigationButtons() {
        // Menu button - navigate to HomeMenuActivity
        btnMenuNav.setOnClickListener(v -> {
            Intent intent = new Intent(FridgeInventoryActivity.this, HomeMenuActivity.class);
            startActivity(intent);
        });

        // Fridge button - already on fridge page
        btnFridgeNav.setOnClickListener(v -> {
            Toast.makeText(this, "You are already on Fridge page", Toast.LENGTH_SHORT).show();
        });

        // Logout button - Commented out, moved to Home Menu Activity Avatar Menu
        // btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Perform logout: clear session and navigate to Login
     */
    private void performLogout() {
        // Clear user session
        UserSessionManager.getInstance(this).clearUserSession();

        // Navigate to Login activity
        Intent intent = new Intent(FridgeInventoryActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        Spinner spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);
        EditText etExpiry = dialogView.findViewById(R.id.etExpiry);
        dialogPreviewImage = dialogView.findViewById(R.id.ivPreview);
        Button btnPickImage = dialogView.findViewById(R.id.btnPickImage);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        pendingImageUri = null;
        final boolean isEditing = food != null;

        tvDialogTitle.setText(isEditing ? "Sửa thực phẩm" : "Thêm thực phẩm");
        btnSave.setText(isEditing ? "Cập nhật" : "Lưu");

        // Load categories and units from database
        dbExecutor.execute(() -> {
            try {
                String userId = UserSessionManager.getInstance(this).getRequiredUserId();
                
                // Fetch categories
                java.util.List<String> categoryNames = new java.util.ArrayList<>();
                java.util.Map<String, String> categoryMap = new java.util.HashMap<>();
                categoryNames.add("-- Chọn danh mục --");
                categoryMap.put("-- Chọn danh mục --", "0");
                
                Connection conn = DatabaseConnection.getConnection();
                String query = "SELECT category_id, category_name FROM category";
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("category_name");
                    String id = rs.getString("category_id");
                    categoryNames.add(name);
                    categoryMap.put(name, id);
                }
                rs.close();
                pstmt.close();
                
                // Fetch units
                java.util.List<String> unitNames = new java.util.ArrayList<>();
                java.util.Map<String, String> unitMap = new java.util.HashMap<>();
                unitNames.add("-- Chọn đơn vị --");
                unitMap.put("-- Chọn đơn vị --", "0");
                
                query = "SELECT unit_id, unit_name FROM unit";
                pstmt = conn.prepareStatement(query);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("unit_name");
                    String id = rs.getString("unit_id");
                    unitNames.add(name);
                    unitMap.put(name, id);
                }
                rs.close();
                pstmt.close();
                
                runOnUiThread(() -> {
                    // Setup Category Spinner
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, categoryNames
                    );
                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(categoryAdapter);
                    
                    // Setup Unit Spinner
                    ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, unitNames
                    );
                    unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerUnit.setAdapter(unitAdapter);
                    
                    // Pre-select if editing
                    if (isEditing && food != null) {
                        etName.setText(food.getFoodName());
                        etQty.setText(String.valueOf(food.getQuantity()));
                        if (food.getExpiryDate() != null) etExpiry.setText(dateFormat.format(food.getExpiryDate()));
                        
                        // Set category spinner
                        if (food.getCategory() != null) {
                            int categoryPos = categoryNames.indexOf(food.getCategory().getCategoryName());
                            if (categoryPos >= 0) spinnerCategory.setSelection(categoryPos);
                        }
                        
                        // Set unit spinner
                        if (food.getUnit() != null) {
                            int unitPos = unitNames.indexOf(food.getUnit().getUnitName());
                            if (unitPos >= 0) spinnerUnit.setSelection(unitPos);
                        }
                        
                        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
                            Glide.with(this).load(food.getImageUrl()).into(dialogPreviewImage);
                        }
                    }
                    
                    // Save button listener with spinner values
                    btnSave.setOnClickListener(v -> {
                        String name = etName.getText().toString().trim();
                        String qtyStr = etQty.getText().toString().trim();
                        String expiryStr = etExpiry.getText().toString().trim();
                        String selectedCategory = spinnerCategory.getSelectedItem().toString();
                        String selectedUnit = spinnerUnit.getSelectedItem().toString();

                        if (!validateInput(name, qtyStr, expiryStr)) return;
                        if (selectedCategory.equals("-- Chọn danh mục --") || selectedUnit.equals("-- Chọn đơn vị --")) {
                            Toast.makeText(FridgeInventoryActivity.this, "Vui lòng chọn danh mục và đơn vị", Toast.LENGTH_SHORT).show();
                            return;
                        }

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
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }

                        if (pendingImageUri != null) {
                            Toast.makeText(this, "Đang upload ảnh...", Toast.LENGTH_SHORT).show();
                            CloudinaryHelper.uploadImage(this, pendingImageUri, new CloudinaryHelper.UploadCallback() {
                                @Override
                                public void onSuccess(String newImageUrl) {
                                    itemToSave.setImageUrl(newImageUrl);
                                    if (isEditing) updateFoodInDb(itemToSave, dbSuccessAction, dbErrorAction, selectedCategory, selectedUnit);
                                    else insertFoodToDb(itemToSave, dbSuccessAction, dbErrorAction, selectedCategory, selectedUnit);
                                }
                                @Override
                                public void onError(String error) {
                                    Toast.makeText(FridgeInventoryActivity.this, "Lỗi upload ảnh: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            if (isEditing) {
                                updateFoodInDb(itemToSave, dbSuccessAction, dbErrorAction, selectedCategory, selectedUnit);
                            } else {
                                itemToSave.setImageUrl(""); // Default image if none picked
                                insertFoodToDb(itemToSave, dbSuccessAction, dbErrorAction, selectedCategory, selectedUnit);
                            }
                        }
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(FridgeInventoryActivity.this, "Lỗi tải danh mục/đơn vị", Toast.LENGTH_SHORT).show();
            }
        });

        btnPickImage.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
            pick.setType("image/*");
            startActivityForResult(Intent.createChooser(pick, "Chọn ảnh"), REQ_PICK_IMAGE);
        });

        etExpiry.setOnClickListener(v -> showDatePicker(etExpiry));

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
