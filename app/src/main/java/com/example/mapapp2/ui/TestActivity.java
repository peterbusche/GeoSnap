package com.example.mapapp2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mapapp2.R;
import com.example.mapapp2.auth.AuthManager;

public class TestActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test); // New layout for TestActivity

        tvWelcome = findViewById(R.id.tv_welcome);
        btnLogout = findViewById(R.id.btn_logout);

        // Get the saved user ID
        int userId = AuthManager.getUserId(this);
        String token = AuthManager.getToken(this);

        if (token == null || userId == -1) {
            Toast.makeText(this, "Unauthorized access! Please log in.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Set welcome message
        tvWelcome.setText("Welcome to TestActivity, User ID: " + userId);

        // Logout button logic
        btnLogout.setOnClickListener(v -> {
            AuthManager.clearAuthData(this);
            Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(TestActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

