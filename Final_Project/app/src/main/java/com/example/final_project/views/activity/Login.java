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
import com.example.final_project.utils.UserSessionManager;
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
            // Add debug button to test database
//            TextView tvDebug = new TextView(this);
//            tvDebug.setText("🔍 TEST DATABASE");
//            tvDebug.setTextColor(Color.RED);
//            tvDebug.setPadding(20, 20, 20, 20);
//            tvDebug.setOnClickListener(v -> {
//                Intent debugIntent = new Intent(Login.this, DatabaseTestActivity.class);
//                startActivity(debugIntent);
//            });
//            ((android.view.ViewGroup) tvErrorMessage.getParent()).addView(tvDebug);

            ExecutorService debugExec = Executors.newSingleThreadExecutor();
            debugExec.execute(() -> {
                try {
                    Thread.sleep(500); // slight delay to let UI settle
                } catch (InterruptedException ignored) {}
                String[][] samples = {
                    {"an@gmail.com", "hashed_pw_1"},
                    {"hoa@gmail.com", "hashed_pw_2"},
                    {"khang@gmail.com", "hashed_pw_3"}
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

            Log.i(TAG, "═══════════════════════════════════════");
            Log.i(TAG, "LOGIN BUTTON CLICKED");
            Log.i(TAG, "Email: [" + email + "]");
            Log.i(TAG, "Password: [" + password + "] (length=" + password.length() + ")");
            Log.i(TAG, "═══════════════════════════════════════");

            tvErrorMessage.setVisibility(View.GONE);
            btnLogin.setEnabled(false);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                int result = checkLoginResultCode(email, password);
                Log.i(TAG, "Login result code: " + result);

                // If login succeeded, fetch and save user session on background thread before navigating
                if (result == LOGIN_SUCCESS) {
                    Log.i(TAG, "Fetching user session...");
                    result = fetchAndSaveUserSession(email) ? LOGIN_SUCCESS : LOGIN_ERROR;
                    Log.i(TAG, "Session fetch result: " + (result == LOGIN_SUCCESS ? "SUCCESS" : "FAILED"));
                }
                
                final int finalResult = result;
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    Log.i(TAG, "Processing UI for result code: " + finalResult);
                    switch (finalResult) {
                        case LOGIN_SUCCESS:
                            Log.i(TAG, "✅ LOGIN SUCCESS - Navigating to HomeMenuActivity");
                            Intent intent = new Intent(Login.this, HomeMenuActivity.class);
                            Toast.makeText(Login.this, "Login successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();
                            break;
                        case LOGIN_NO_CONNECTION:
                            Log.e(TAG, "❌ LOGIN FAILED - No database connection");
                            tvErrorMessage.setText("Không thể kết nối cơ sở dữ liệu. Vui lòng kiểm tra cấu hình.");
                            tvErrorMessage.setVisibility(View.VISIBLE);
                            break;
                        case LOGIN_NO_USER:
                            Log.w(TAG, "❌ LOGIN FAILED - User not found");
                            tvErrorMessage.setText("Email không tồn tại.");
                            tvErrorMessage.setVisibility(View.VISIBLE);
                            break;
                        case LOGIN_WRONG_PASSWORD:
                            Log.w(TAG, "❌ LOGIN FAILED - Wrong password");
                            tvErrorMessage.setText("Sai mật khẩu.");
                            tvErrorMessage.setVisibility(View.VISIBLE);
                            break;
                        default:
                            Log.e(TAG, "❌ LOGIN FAILED - Unknown error");
                            tvErrorMessage.setText("Đăng nhập thất bại. Vui lòng thử lại.");
                            tvErrorMessage.setVisibility(View.VISIBLE);
                            break;
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

                        // Trim whitespace from DB password
                        dbPassword = dbPassword.trim();
                        String inputPassword = password.trim();

                        // Log for debugging
                        Log.d(TAG, "Password comparison:");
                        Log.d(TAG, "  Input: [" + inputPassword + "] (length=" + inputPassword.length() + ")");
                        Log.d(TAG, "  DB:    [" + dbPassword + "] (length=" + dbPassword.length() + ")");

                        // Direct comparison
                        if (inputPassword.equals(dbPassword)) {
                            Log.d(TAG, "Login attempt for " + email + " result=SUCCESS");
                            return LOGIN_SUCCESS;
                        } else {
                            Log.d(TAG, "Login attempt for " + email + " result=WRONG_PASSWORD");
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

    /**
     * Fetch user details from database and save to session
     * Returns true if session was saved successfully, false otherwise
     */
    private boolean fetchAndSaveUserSession(String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                Log.e(TAG, "Cannot fetch user details: DB connection is null");
                return false;
            }
            String sql = "SELECT user_id, fullname FROM `User` WHERE LOWER(email) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String userId = rs.getString("user_id");
                        String fullName = rs.getString("fullname");
                        // Save to UserSessionManager
                        UserSessionManager.getInstance(this).saveUserSession(userId, email, fullName);
                        Log.d(TAG, "User session saved: userId=" + userId + ", email=" + email);
                        return true;
                    } else {
                        Log.e(TAG, "User not found in database for email: " + email);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching user details", e);
            return false;
        }
    }
}
