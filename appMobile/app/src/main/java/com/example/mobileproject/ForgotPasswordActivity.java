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
import java.net.URLEncoder;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "🔥 quan 🔥";
    private EditText edtPhone;
    private Button btnContinue;

    private static final String API_KEY = "ae570ffe";
    private static final String API_SECRET = "jqBpx3nYpjc6DerO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quenmatkhau);

        edtPhone = findViewById(R.id.register_phone);
        btnContinue = findViewById(R.id.register_btn_createaccount);

        btnContinue.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }

            checkPhoneNumber(phone);
        });
    }

    private void checkPhoneNumber(String phone) {
        new Thread(() -> {
            try {
                String checkUrl = getString(R.string.base_url) + "/password-recovery/check-phone";
                Log.d(TAG, "🔥 Sending POST to " + checkUrl);
                URL url = new URL(checkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject jsonInput = new JSONObject();
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
                        String code = String.format("%04d", new Random().nextInt(10000));
                        Log.d(TAG, "🔥 Generated code: " + code);
                        guitinnhan(phone, code);
                    } else {
                        try {
                            JSONObject errorJson = new JSONObject(jsonResponse);
                            String errorMessage = errorJson.optString("detail", "Lỗi kiểm tra số điện thoại");
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(this, "Lỗi kiểm tra số điện thoại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Network error: ", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void guitinnhan(String phone, String code) {
        new Thread(() -> {
            try {
                String formattedPhone = phone.startsWith("0") ? "+84" + phone.substring(1) : phone;
                Log.d(TAG, "🔥 Attempting to send SMS to: " + formattedPhone);

                URL url = new URL("https://rest.nexmo.com/sms/json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String message = "Mã xác nhận của bạn là: " + code;
                String body = "api_key=" + URLEncoder.encode(API_KEY, "UTF-8") +
                        "&api_secret=" + URLEncoder.encode(API_SECRET, "UTF-8") +
                        "&to=" + URLEncoder.encode(formattedPhone, "UTF-8") +
                        "&from=" + URLEncoder.encode("VonageAPI", "UTF-8") +
                        "&text=" + URLEncoder.encode(message, "UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "🔥 SMS Response code: " + responseCode);

                InputStream inputStream = (responseCode == 200)
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
                Log.d(TAG, "🔥 SMS Response: " + jsonResponse);

                JSONObject json = new JSONObject(jsonResponse);
                JSONObject messageDetails = json.getJSONArray("messages").getJSONObject(0);
                String status = messageDetails.getString("status");
                String errorText = messageDetails.optString("error-text", "Unknown error");

                runOnUiThread(() -> {
                    if ("0".equals(status)) {
                        Log.d(TAG, "🔥 SMS sent successfully");
                        Toast.makeText(this, "Đã gửi SMS với mã xác nhận", Toast.LENGTH_SHORT).show();
                        proceedToEnterCode(phone, code);
                    } else {
                        Log.e(TAG, "🔥 SMS failed: " + errorText);
                        Toast.makeText(this, "Không thể gửi SMS: " + errorText + ". Mã xác nhận: " + code, Toast.LENGTH_LONG).show();
                        proceedToEnterCode(phone, code);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "🔥 SMS failed: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không thể gửi SMS: " + e.getMessage() + ". Mã xác nhận: " + code, Toast.LENGTH_LONG).show();
                    proceedToEnterCode(phone, code);
                });
            }
        }).start();
    }

    private void proceedToEnterCode(String phone, String code) {
        Intent intent = new Intent(this, EnterCodeActivity.class);
        intent.putExtra("phone", phone);
        intent.putExtra("code", code);
        startActivity(intent);
    }
}