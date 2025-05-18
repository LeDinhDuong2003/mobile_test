package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ResetPasswordActivity extends AppCompatActivity {
    private static final String TAG = "🔥 quan 🔥";
    private EditText edtNewPassword, edtConfirmPassword;
    private Button btnContinue;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.laylaimatkhau);

        // Lấy phone từ Intent
        phone = getIntent().getStringExtra("phone");

        // Liên kết với layout
        edtNewPassword = findViewById(R.id.getpassword_newpassword);
        edtConfirmPassword = findViewById(R.id.getpassword_confirmpassword);
        btnContinue = findViewById(R.id.getpassword_btn_continue);

        // Xử lý nút Continue
        btnContinue.setOnClickListener(v -> {
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            // Kiểm tra mật khẩu
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gửi request tới FastAPI
            new Thread(() -> {
                try {
                    String resetUrl = getString(R.string.base_url) + "/password-recovery/reset";
                    Log.d(TAG, "🔥 Sending POST to " + resetUrl);
                    URL url = new URL(resetUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("phone", phone);
                    jsonInput.put("new_password", newPassword);
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
                            Toast.makeText(this, "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, PasswordRecoverSuccessActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                JSONObject errorJson = new JSONObject(jsonResponse);
                                String errorMessage = errorJson.optString("detail", "Lỗi đặt lại mật khẩu");
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Toast.makeText(this, "Lỗi đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
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
    }
}