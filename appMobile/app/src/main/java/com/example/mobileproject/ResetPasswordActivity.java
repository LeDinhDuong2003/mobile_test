package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.User;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

            // Tạo đối tượng User cho request
            User resetUser = new User();
            resetUser.setPhone(phone);
            resetUser.setPassword(newPassword);
            Log.d(TAG, "🔥 Request body: { phone: "+phone+", new_password: "+newPassword+" }");

            // Gọi API bằng Retrofit
            ApiService apiService = RetrofitClient.getClient();
            Call<ResponseBody> call = apiService.resetPassword(resetUser);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ResetPasswordActivity.this,
                                "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ResetPasswordActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = "Lỗi đặt lại mật khẩu";
                        try {
                            if (response.errorBody() != null) {
                                JSONObject errorJson = new JSONObject(response.errorBody().string());
                                errorMessage = errorJson.optString("detail", errorMessage);
                                Log.e(TAG, "🔥 Server error response: " + errorJson.toString());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response: ", e);
                        }
                        Toast.makeText(ResetPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Network error: ", t);
                    Toast.makeText(ResetPasswordActivity.this, "Lỗi kết nối: " +
                            t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}