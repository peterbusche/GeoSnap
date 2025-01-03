package com.example.mapapp2.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mapapp2.R;
import com.example.mapapp2.network.ApiClient;
import com.example.mapapp2.network.ApiService;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etPasswordConfirmation;
    private Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirmation = findViewById(R.id.et_password_confirmation);
        btnCreateAccount = findViewById(R.id.btn_create_account);

        btnCreateAccount.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirmation = etPasswordConfirmation.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirmation.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordConfirmation)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Wrap parameters in user key for ruby backend
        Map<String, Object> userParams = new HashMap<>();
        userParams.put("username", username);
        userParams.put("email", email);
        userParams.put("password", password);
        userParams.put("password_confirmation", passwordConfirmation);

        Map<String, Object> payload = new HashMap<>();
        payload.put("user", userParams);

        Call<Void> call = apiService.createUser(payload);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateAccountActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to login screen
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        JSONObject jsonError = new JSONObject(errorBody);
                        String errorMessage = jsonError.optString("errors", "Failed to create account");
                        Toast.makeText(CreateAccountActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("CreateAccountActivity", "Error parsing error response", e);
                        Toast.makeText(CreateAccountActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CreateAccountActivity", "API call failed", t);
                Toast.makeText(CreateAccountActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

