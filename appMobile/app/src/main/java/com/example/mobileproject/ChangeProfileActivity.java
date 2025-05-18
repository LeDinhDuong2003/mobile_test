package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChangeProfileActivity extends AppCompatActivity {
    private static final String TAG = "🔥 quan 🔥";
    private static final int USER_ID = 1;
    private static final int CHANGE_PASSWORD_REQUEST_CODE = 103;
    private EditText profileChangeName, profileChangeEmail, profileChangePhone;
    private Button btnSave, btnCancel;
    private ImageView btnEditPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thaydoihoso);

        profileChangeName = findViewById(R.id.profilechange_name);
        profileChangeEmail = findViewById(R.id.profilechange_email);
        profileChangePhone = findViewById(R.id.profilechange_phone);
        btnSave = findViewById(R.id.profilechange_btnsave);
        btnCancel = findViewById(R.id.profilechange_btncancel);
        btnEditPassword = findViewById(R.id.profile_btnedit);

        // Nhận dữ liệu từ InformationActivity
        Intent intent = getIntent();
        profileChangeName.setText(intent.getStringExtra("name"));
        profileChangeEmail.setText(intent.getStringExtra("email"));
        profileChangePhone.setText(intent.getStringExtra("phone"));

        // Cho phép chỉnh sửa
        profileChangeName.setEnabled(true);
        profileChangeEmail.setEnabled(true);
        profileChangePhone.setEnabled(true);

        btnEditPassword.setOnClickListener(v -> {
            Intent passwordIntent = new Intent(this, ChangePasswordActivity.class);
            startActivityForResult(passwordIntent, CHANGE_PASSWORD_REQUEST_CODE);
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnSave.setOnClickListener(v -> {
            String name = profileChangeName.getText().toString().trim();
            String email = profileChangeEmail.getText().toString().trim();
            String phone = profileChangePhone.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            updateProfile(name, email, phone);
        });
    }

    private void updateProfile(String name, String email, String phone) {
        new Thread(() -> {
            try {
                String apiUrl = getString(R.string.base_url) + "/update-profile";
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                JSONObject json = new JSONObject();
                json.put("user_id", USER_ID);
                json.put("full_name", name);
                json.put("email", email);
                json.put("phone", phone);

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
                Log.d(TAG, "🔥 Update profile response: " + jsonResponse);

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResult = new JSONObject(jsonResponse);
                        if (responseCode >= 200 && responseCode < 300) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("name", name);
                            resultIntent.putExtra("email", email);
                            resultIntent.putExtra("phone", phone);
                            setResult(RESULT_OK, resultIntent);
                            Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Lỗi: " + jsonResult.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "🔥 JSON parse error: ", e);
                        Toast.makeText(this, "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "🔥 Update profile error: ", e);
                runOnUiThread(() -> Toast.makeText(this, "Lỗi cập nhật thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_PASSWORD_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
        }
    }
}