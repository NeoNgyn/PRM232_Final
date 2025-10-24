package com.example.final_project.views.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.final_project.R;
import com.example.final_project.utils.DatabaseConnection;
import com.example.final_project.BuildConfig;
import com.example.final_project.views.activity.HomeMenuActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Login extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageView btnBack, btnTogglePassword;

    private TextView tvSignUp;
    private TextView tvErrorMessage;
    private boolean isPasswordVisible = false;

    // result codes
    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_NO_CONNECTION = 1;
    private static final int LOGIN_NO_USER = 2;
    private static final int LOGIN_WRONG_PASSWORD = 3;
    private static final int LOGIN_ERROR = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Ánh xạ view
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBack = findViewById(R.id.btnBack);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        // 1. Quay lại Startedscreen khi bấm Back
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, StartedScreen.class);
            startActivity(intent);
            finish();
        });

        // 2. Toggle password visibility
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
            etPassword.setSelection(etPassword.length()); // đưa con trỏ về cuối
        });

        // 3. Disable login button nếu chưa nhập đủ
        btnLogin.setEnabled(false);
        btnLogin.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.darker_gray));
        btnLogin.setTextColor(Color.BLACK);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình Login
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });

        // DEBUG automatic test: try sample users when running debug build
        if (BuildConfig.DEBUG) {
            ExecutorService debugExec = Executors.newSingleThreadExecutor();
            debugExec.execute(() -> {
                try {
                    Thread.sleep(500); // slight delay to let UI settle
                } catch (InterruptedException ignored) {}
                String[][] samples = {
                    {"alice.j@example.com", "password123"},
                    {"bob.smith@example.com", "securepass"},
                    {"an.tran@example.com", "anpass456"}
                };
                for (String[] s : samples) {
                    int res = checkLoginResultCode(s[0], s[1]);
                    Log.i(TAG, "DEBUG LOGIN TEST: email=" + s[0] + " resultCode=" + res);
                }
            });
        }

        // Sự kiện Login: run DB check on background thread
        btnLogin.setOnClickListener(v -> {
            final String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();

            tvErrorMessage.setVisibility(View.GONE);
            btnLogin.setEnabled(false);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                int result = checkLoginResultCode(email, password);
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    switch (result) {
                        case LOGIN_SUCCESS:
                            Intent intent = new Intent(Login.this, HomeMenuActivity.class);
                            Toast.makeText(Login.this, "Login successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();
                            break;
                        case LOGIN_NO_CONNECTION:
                            tvErrorMessage.setText("Không thể kết nối cơ sở dữ liệu. Vui lòng kiểm tra cấu hình.");
                            tvErrorMessage.setVisibility(View.VISIBLE);
                            break;
                        case LOGIN_NO_USER:
                            tvErrorMessage.setText("Email không tồn tại.");
                            tvErrorMessage.setVisibility(View.VISIBLE);
                            break;
                        case LOGIN_WRONG_PASSWORD:
                            tvErrorMessage.setText("Sai mật khẩu.");
                            tvErrorMessage.setVisibility(View.VISIBLE);
                            break;
                        default:
                            tvErrorMessage.setText("Đăng nhập thất bại. Vui lòng thử lại.");
                            tvErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
            });
        });
    }

    // Kiểm tra 2 field
    private void validateFields() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!email.isEmpty() && !password.isEmpty()) {
            btnLogin.setEnabled(true);
            btnLogin.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.orange));
            btnLogin.setTextColor(Color.BLACK);
        } else {
            btnLogin.setEnabled(false);
            btnLogin.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.darker_gray)); // xám
            btnLogin.setTextColor(Color.BLACK);
        }
    }

    // Returns result code instead of boolean for better diagnostics
    private int checkLoginResultCode(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null) return LOGIN_ERROR;
        String sql = "SELECT password FROM `User` WHERE LOWER(email) = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                Log.e(TAG, "Database connection is null");
                return LOGIN_NO_CONNECTION;
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String emailParam = email.trim().toLowerCase();
                Log.d(TAG, "Checking login for email(lower)=" + emailParam);
                stmt.setString(1, emailParam);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String dbPassword = rs.getString("password");
                        if (dbPassword == null || dbPassword.isEmpty()) {
                            Log.e(TAG, "No password stored for email=" + email);
                            return LOGIN_ERROR;
                        }
                        dbPassword = dbPassword.trim();
                        if (dbPassword.startsWith("\"") && dbPassword.endsWith("\"")) {
                            dbPassword = dbPassword.substring(1, dbPassword.length()-1);
                        }
                        if (dbPassword.startsWith("'") && dbPassword.endsWith("'")) {
                            dbPassword = dbPassword.substring(1, dbPassword.length()-1);
                        }
                        dbPassword = dbPassword.replaceAll("\\\\", "");
                        dbPassword = dbPassword.replaceAll("[^\\x20-\\x7E]", "");
                        // So sánh trực tiếp với mật khẩu nhập vào
                        if (password.equals(dbPassword)) {
                            Log.d(TAG, "Login attempt for " + email + " result=true");
                            return LOGIN_SUCCESS;
                        } else {
                            Log.d(TAG, "Login attempt for " + email + " result=false");
                            return LOGIN_WRONG_PASSWORD;
                        }
                    } else {
                        Log.d(TAG, "No user found with email=" + email);
                        return LOGIN_NO_USER;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during login", e);
            return LOGIN_ERROR;
        }
    }
}
