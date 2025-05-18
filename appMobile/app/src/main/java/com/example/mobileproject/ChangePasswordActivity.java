package com.example.mobileproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "🔥 quan 🔥";
    private static final int USER_ID = 1;
    private EditText password, newPassword, confirmPassword;
    private Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thaydoimatkhau);

        password = findViewById(R.id.changepassword_password);
        newPassword = findViewById(R.id.changepassword_newpassword);
        confirmPassword = findViewById(R.id.changepassword_confirmpassword);
        btnSave = findViewById(R.id.changepassword_btnsave);
        btnCancel = findViewById(R.id.changepassword_btncancel);

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnSave.setOnClickListener(v -> {
            String currentPassword = password.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu mới và xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(currentPassword, newPass);
        });
    }

    private void changePassword(String currentPassword, String newPassword) {
        new Thread(() -> {
            try {
                String apiUrl = getString(R.string.base_url) + "/change-password";
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                JSONObject json = new JSONObject();
                json.put("user_id", USER_ID);
                json.put("current_password", currentPassword);
                json.put("new_password", newPassword);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                BufferedReader reader;
                if (responseCode >= 200 && responseCode < 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                String jsonResponse = response.toString();
                Log.d(TAG, "🔥 Change password response: " + jsonResponse);

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResult = new JSONObject(jsonResponse);
                        if (responseCode >= 200 && responseCode < 300) {
                            setResult(RESULT_OK);
                            Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // Kiểm tra trường "detail" và lấy "error" bên trong
                            String errorMessage = "Lỗi không xác định từ server";
                            if (jsonResult.has("detail")) {
                                Object detail = jsonResult.get("detail");
                                if (detail instanceof JSONObject) {
                                    JSONObject detailObj = (JSONObject) detail;
                                    if (detailObj.has("error")) {
                                        errorMessage = detailObj.getString("error");
                                    }
                                } else if (detail instanceof String) {
                                    errorMessage = (String) detail;
                                }
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "🔥 JSON parse error: ", e);
                        Toast.makeText(this, "Lỗi phản hồi từ server: " + jsonResponse, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "🔥 Change password error: ", e);
                runOnUiThread(() -> Toast.makeText(this, "Lỗi kết nối server: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}