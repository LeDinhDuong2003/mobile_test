package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileproject.model.User;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "🔥 quan 🔥";
    private EditText edtUsername, edtEmail, edtPassword, edtPhone;
    private CheckBox cbAgree;
    private Button btnCreateAccount;
    private TextView tvSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangky);

        // Liên kết với layout
        edtUsername = findViewById(R.id.register_username);
        edtEmail = findViewById(R.id.register_email);
        edtPassword = findViewById(R.id.register_password);
        edtPhone = findViewById(R.id.register_phone);
        cbAgree = findViewById(R.id.cb_agree);
        btnCreateAccount = findViewById(R.id.register_btn_createaccount);
        tvSignIn = findViewById(R.id.register_signin);

        // Xử lý nút Create Account
        btnCreateAccount.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            // Kiểm tra dữ liệu
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!cbAgree.isChecked()) {
                Toast.makeText(this, "Vui lòng đồng ý với điều khoản dịch vụ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gửi request tới FastAPI
            new Thread(() -> {
                try {
                    String registerUrl = getString(R.string.base_url) + "/auth/register";
                    Log.d(TAG, "🔥 Sending POST to " + registerUrl);
                    URL url = new URL(registerUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("username", username);
                    jsonInput.put("email", email);
                    jsonInput.put("password", password);
                    jsonInput.put("phone", phone);
                    String jsonString = jsonInput.toString();
                    Log.d(TAG, "🔥 Request body: " + jsonString);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "🔥 Response code: " + responseCode);

                    InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                            ? conn.getInputStream()
                            : conn.getErrorStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    conn.disconnect();

                    String jsonResponse = response.toString();
                    Log.d(TAG, "🔥 Server response: " + jsonResponse);

                    runOnUiThread(() -> {
                        if (responseCode >= 200 && responseCode < 300) {
                            try {
                                JSONObject jsonObject = new JSONObject(jsonResponse);
                                User user = new User();
                                user.setUserId(jsonObject.getInt("user_id"));
                                user.setFullName(jsonObject.getString("full_name"));
                                user.setEmail(jsonObject.getString("email"));
                                user.setPhone(jsonObject.optString("phone", null));
                                user.setAvatarUrl(jsonObject.optString("avatar_url", null));
                                user.setGoogleId(jsonObject.optString("google_id", null));
                                user.setRole(jsonObject.optString("role", null));

                                Toast.makeText(this, "Đăng ký thành công: " + user.getFullName(), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(this, MainActivityHomePage.class);
                                startActivity(intent);
                                finish();
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON parse error: ", e);
                                Toast.makeText(this, "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                JSONObject errorJson = new JSONObject(jsonResponse);
                                String errorMessage = errorJson.optString("detail", "Lỗi đăng ký");
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Toast.makeText(this, "Lỗi đăng ký", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Network error: ", e);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        // Xử lý nút Sign in
        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}