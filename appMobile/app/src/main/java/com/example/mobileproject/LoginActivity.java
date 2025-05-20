package com.example.mobileproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        setContentView(R.layout.dangnhap);

        edtUsername = findViewById(R.id.login_username);
        edtPassword = findViewById(R.id.login_password);
        btnSignIn = findViewById(R.id.login_btn_sign_in);
        btnGoogle = findViewById(R.id.login_btn_google);
        txtForgotPassword = findViewById(R.id.login_forgot_password);
        txtSignUp = findViewById(R.id.login_signup);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(CLIENT_ID)
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

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

        btnSignIn.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            Log.d(TAG, "🔥 Attempting login with username: " + username);
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập username và password", Toast.LENGTH_SHORT).show();
                return;
            }
            User loginUser = new User();
            loginUser.setUsername(username);
            loginUser.setPassword(password);

            // Gọi API bằng Retrofit
            ApiService apiService = RetrofitClient.getClient();
            Call<User> call = apiService.login(loginUser);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công: " + user.getFullName(), Toast.LENGTH_LONG).show();
                        Log.d(TAG, "🔥 User ID: " + user.getUserId());
                        saveUserToSharedPreferences(user);
                        Intent intent = new Intent(LoginActivity.this, MainActivityHomePage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = "Sai tài khoản hoặc mật khẩu";
                        try {
                            if (response.errorBody() != null) {
                                JSONObject errorJson = new JSONObject(response.errorBody().string());
                                errorMessage = errorJson.optString("detail", errorMessage);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response: ", e);
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "Network error: ", t);
                    Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Đăng nhập bằng Google
//        btnGoogle.setOnClickListener(v -> {
//            Intent signInIntent = googleSignInClient.getSignInIntent();
//            googleSignInLauncher.launch(signInIntent);
//        });
        btnGoogle.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });

        txtForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

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
        editor.putString("role",user.getRole());
        editor.apply();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String email = account.getEmail();
            String googleId = account.getId();
            String displayName = account.getDisplayName();
            String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
            Log.d(TAG, "🔥 Google Sign-In: email=" + email + ", googleId=" + googleId + ", displayName=" + displayName + ", photoUrl=" + photoUrl);

            User loginUser = new User();
            loginUser.setGoogleId(googleId);
            loginUser.setEmail(email);
            loginUser.setFullName(displayName);
            loginUser.setAvatarUrl(photoUrl);

            ApiService apiService = RetrofitClient.getClient();
            Call<User> call = apiService.googleLogin(loginUser);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        Toast.makeText(LoginActivity.this, "Đăng nhập Google thành công: " + user.getFullName(), Toast.LENGTH_LONG).show();
                        Log.d(TAG, "🔥 User ID: " + user.getUserId());
                        saveUserToSharedPreferences(user);

                        Intent intent = new Intent(LoginActivity.this, MainActivityHomePage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = "Lỗi xác thực Google";
                        try {
                            if (response.errorBody() != null) {
                                JSONObject errorJson = new JSONObject(response.errorBody().string());
                                errorMessage = errorJson.optString("detail", errorMessage);
                                Log.e(TAG, "🔥 Server error response: " + errorJson.toString());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response: ", e);
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "Network error: ", t);
                    Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (ApiException e) {
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode(), e);
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
        }
    }

}
