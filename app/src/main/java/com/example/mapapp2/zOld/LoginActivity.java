package com.example.mapapp2.zOld;

//anroid imports
import android.util.Log;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.Button;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.widget.EditText;

//java


//google imports
import com.example.mapapp2.R;

//API imports
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class LoginActivity extends AppCompatActivity {
    private static String TAG = "MapAppLogs";
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Here at Login Activity");
        setContentView(R.layout.activity_login); // Create this layout file

        apiService = ApiClient.getClient().create(ApiService.class);

        Button loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        EditText usernameField = findViewById(R.id.et_username);
        EditText passwordField = findViewById(R.id.et_password);

        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<LoginResponse> call = apiService.login(username, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    int userId = response.body().getUserId();

                    // Save token and userId in SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("token", token);
                    editor.putInt("user_id", userId);
                    editor.apply();

                    // Navigate to MapsActivity
                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

