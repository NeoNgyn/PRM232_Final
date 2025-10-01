package com.example.final_project;

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageView btnBack, btnTogglePassword;

    private TextView tvSignUp;
    private TextView tvErrorMessage;
    private boolean isPasswordVisible = false;

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

        // Sự kiện Login test login thành công
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.equals("admin@gmail.com") && password.equals("123456")) {
                // Đăng nhập thành công → chuyển sang màn hình khác
                Intent intent = new Intent(Login.this, StartedScreen.class);
                Toast.makeText(Login.this, "Login successfully!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
            } else {
                // Sai tài khoản → hiện thông báo
                tvErrorMessage.setVisibility(View.VISIBLE);
            }
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
}

