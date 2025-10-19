package com.example.final_project.views.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.final_project.R;
import com.example.final_project.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class SignUp extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword;
    private CheckBox cbTerms;
    private Button btnCreateAccount;
    private TextView tvLogin;
    private ImageView btnBack, btnTogglePassword;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Ánh xạ view
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLogin = findViewById(R.id.tvLogin);
        btnBack = findViewById(R.id.btnBack);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);

        // Quay lại màn Login
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, Login.class);
            startActivity(intent);
            finish();
        });

        // Toggle mật khẩu
        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_closed);
                isPasswordVisible = false;
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_open);
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.length());
        });

        // Validate realtime
        setupValidation();

        // Checkbox
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCheckboxColor(isChecked);
            validateFields();
        });
        updateCheckboxColor(cbTerms.isChecked());

        // Chuyển sang màn Login khi bấm "Login"
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, Login.class);
            startActivity(intent);
            finish();
        });

        btnCreateAccount.setOnClickListener(v -> {
            String fullname = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (fullname.isEmpty() || email.isEmpty() || password.isEmpty() || !cbTerms.isChecked()) {
                return;
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                final boolean emailExists = isEmailExists(email);

                if (emailExists) {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(SignUp.this, "Email already exists!", android.widget.Toast.LENGTH_SHORT).show();
                    });
                    return;
                }


                String userId = generateNewUserId();
                final boolean userInserted = insertUser(userId, fullname, email, password);

                runOnUiThread(() -> {
                    if (userInserted) {
                        android.widget.Toast.makeText(SignUp.this, "Account created successfully!", android.widget.Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUp.this, Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        android.widget.Toast.makeText(SignUp.this, "Failed to create account. Please try again.", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        validateFields();
    }

    private void updateCheckboxColor(boolean isChecked) {
        if (isChecked) {
            cbTerms.setButtonTintList(ContextCompat.getColorStateList(this, R.color.orange));
        } else {
            cbTerms.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }
    }

    private void validateFields() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean allFilled = !name.isEmpty() && !email.isEmpty() && !password.isEmpty() && cbTerms.isChecked();

        if (allFilled) {
            btnCreateAccount.setEnabled(true);
            btnCreateAccount.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.orange));
            btnCreateAccount.setTextColor(Color.BLACK);
        } else {
            btnCreateAccount.setEnabled(false);
            btnCreateAccount.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D9D9D9")));
            btnCreateAccount.setTextColor(Color.parseColor("#888888"));
        }
    }

    private void setupValidation() {
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateFullName(); validateFields(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateEmail(); validateFields(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validatePassword(); validateFields(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // gia lap api de test create tra ra login
        /*btnCreateAccount.setOnClickListener(v -> {
            // Show loading giả lập
            ProgressDialog progressDialog = new ProgressDialog(SignUp.this);
            progressDialog.setMessage("Creating your account...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Giả lập gọi API 2s
            new android.os.Handler().postDelayed(() -> {
                progressDialog.dismiss();

                // Giả lập response OK
                boolean isSuccess = true; // bạn có thể set false để test lỗi

                if (isSuccess) {
                    // Thông báo
                    Toast.makeText(SignUp.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                    // Chuyển sang Login
                    Intent intent = new Intent(SignUp.this, Login.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Lỗi
                    Toast.makeText(SignUp.this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show();
                }

            }, 2000); // delay 2s cho giống call API
        });*/
    }

    private void validateFullName() {
        String name = etFullName.getText().toString().trim();
        if (name.isEmpty()) {
            setDefaultUI(etFullName);
        } else if (name.length() >= 3 && name.length() <= 20 && name.matches("^[a-zA-Z ]+$")) {
            setValidUI(etFullName);
        } else {
            setInvalidUI(etFullName);
        }
    }

    private void validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            setDefaultUI(etEmail);
        } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && email.toLowerCase().endsWith("@gmail.com")) {
            // Email hợp lệ và phải có đuôi @gmail.com
            setValidUI(etEmail);
        } else {
            setInvalidUI(etEmail);
        }
    }

    private void validatePassword() {
        String password = etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            setDefaultUI(etPassword);
        } else if (password.length() >= 8) {
            setValidUI(etPassword);
        } else {
            setInvalidUI(etPassword);
        }
    }

    private void setValidUI(EditText editText) {
        editText.setBackgroundResource(R.drawable.bg_edittext_green);
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.success, 0);
    }

    private void setInvalidUI(EditText editText) {
        editText.setBackgroundResource(R.drawable.bg_edittext_red);
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.warning, 0);
    }

    private void setDefaultUI(EditText editText) {
        editText.setBackgroundResource(R.drawable.bg_edittext_default);
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    // Kiểm tra email đã tồn tại chưa
    private boolean isEmailExists(String email) {
        String sql = "SELECT user_id FROM User WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Tạo user_id mới
    private String generateNewUserId() {
        String sql = "SELECT MAX(user_id) AS max_id FROM User";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            String newId = "U001";
            if (rs.next()) {
                String maxId = rs.getString("max_id");
                if (maxId != null && maxId.startsWith("U")) {
                    int num = Integer.parseInt(maxId.substring(1)) + 1;
                    newId = String.format("U%03d", num);
                }
            }
            return newId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "U001";
    }

    // Thêm user mới vào database
    private boolean insertUser(String userId, String fullname, String email, String hashedPassword) {
        String sql = "INSERT INTO User (user_id, fullname, email, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, fullname);
            stmt.setString(3, email);
            stmt.setString(4, hashedPassword);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
