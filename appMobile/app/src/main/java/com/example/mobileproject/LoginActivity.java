package com.example.mobileproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileproject.model.User;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnSignIn;
    private LinearLayout btnGoogle;
    private TextView txtForgotPassword, txtSignUp;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private static final String TAG = "🔥 quan 🔥";
    private static final String CLIENT_ID = "466919745508-kdrtei827o3e2ml0tbdsjsb794s8ku6c.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nếu người dùng đã đăng nhập thì chuyển luôn sang MainActivity
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
            return;
        }

        setContentView(R.layout.dangnhap);

        // Ánh xạ view
        edtUsername = findViewById(R.id.login_username);
        edtPassword = findViewById(R.id.login_password);
        btnSignIn = findViewById(R.id.login_btn_sign_in);
        btnGoogle = findViewById(R.id.login_btn_google);
        txtForgotPassword = findViewById(R.id.login_forgot_password);
        txtSignUp = findViewById(R.id.login_signup);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(CLIENT_ID)
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Khởi tạo launcher cho Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        Toast.makeText(this, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

        // LoginActivity.java
        btnSignIn.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            Log.d(TAG, "🔥 Attempting login with username: " + username);
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập username và password", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    Log.d(TAG, "🔥 Sending POST to http://192.168.0.100:8000/auth/login");
                    URL url = new URL(getString(R.string.base_url) + "/auth/login");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(5000); // Timeout 5s
                    conn.setReadTimeout(5000);

                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("username", username);
                    jsonInput.put("password", password);
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
                                Toast.makeText(this, "Đăng nhập thành công: " + user.getFullName(), Toast.LENGTH_LONG).show();
                                Log.d(TAG, "🔥 User ID: " + user.getUserId());
                                saveUserToSharedPreferences(user);
                                Intent intent = new Intent(this, MainActivityHomePage.class);
                                startActivity(intent);
                                finish();
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON parse error: ", e);
                                Toast.makeText(this, "Lỗi phân tích dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                JSONObject errorJson = new JSONObject(jsonResponse);
                                String errorMessage = errorJson.optString("detail", "Sai tài khoản hoặc mật khẩu");
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
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



        // Đăng nhập bằng Google
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Quên mật khẩu
        txtForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Chuyển sang Sign Up
        txtSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void saveUserToSharedPreferences(User user) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("user_id", user.getUserId());
        editor.putString("full_name", user.getFullName());
        editor.putString("email", user.getEmail());
        editor.putString("avatar_url", user.getAvatarUrl());
        editor.putString("phone", user.getPhone());
        editor.apply();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String email = account.getEmail();
            String googleId = account.getId(); // Dùng getId() làm google_id
            String displayName = account.getDisplayName();
            String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
            Log.d(TAG, "🔥 Google Sign-In: email=" + email + ", googleId=" +
                    googleId + ", displayName=" + displayName + ", photoUrl=" + photoUrl);

            // Gửi dữ liệu tới FastAPI
            new Thread(() -> {
                try {
                    String googleLoginUrl = getString(R.string.base_url) + "/auth/google";
                    Log.d(TAG, "🔥 Sending POST to " + googleLoginUrl);
                    URL url = new URL(googleLoginUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("google_id", googleId);
                    jsonInput.put("email", email);
                    jsonInput.put("display_name", displayName);
                    jsonInput.put("photo_url", photoUrl);
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
                                Toast.makeText(this, "Đăng nhập Google thành công: " +
                                        user.getFullName(), Toast.LENGTH_LONG).show();
                                saveUserToSharedPreferences(user);
//                                Intent intent = new Intent(this, MainActivity.class);
//                                startActivity(intent);
//                                finish();
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON parse error: ", e);
                                Toast.makeText(this, "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                JSONObject errorJson = new JSONObject(jsonResponse);
                                String errorMessage = errorJson.optString("detail", "Lỗi xác thực Google");
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Toast.makeText(this, "Lỗi xác thực Google", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Network error: ", e);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();

        } catch (ApiException e) {
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode(), e);
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
        }
    }

}
