package com.example.final_project.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.utils.UserSessionManager;

public class StartedScreen extends AppCompatActivity {

    private Button btnGetStarted;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        UserSessionManager sessionManager = UserSessionManager.getInstance(this);
        if (sessionManager.isLoggedIn()) {
            // User is already logged in, skip to HomeMenuActivity
            Intent intent = new Intent(StartedScreen.this, HomeMenuActivity.class);
            startActivity(intent);
            finish(); // Close StartedScreen so user can't go back
            return; // Important: stop further execution
        }

        setContentView(R.layout.startedscreen);

        // Ánh xạ view
        btnGetStarted = findViewById(R.id.btnGetStarted);
        tvLogin = findViewById(R.id.tvLogin);

       // Xử lý sự kiện bấm Get Started
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình Login
                Intent intent = new Intent(StartedScreen.this, Login.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện bấm Login
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình Login
                Intent intent = new Intent(StartedScreen.this, Login.class);
                startActivity(intent);
            }
        });
    }
}