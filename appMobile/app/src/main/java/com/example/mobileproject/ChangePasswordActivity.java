package com.example.mobileproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.ChangePassword;
import org.json.JSONObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "🔥 quan 🔥";
    private EditText password, newPassword, confirmPassword;
    private Button btnSave, btnCancel;
    private SharedPreferences sharedPreferences;
    private int USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thaydoimatkhau);

        sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        USER_ID = sharedPreferences.getInt("user_id", 1);
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
            if (newPass.length() > 30) {
                Toast.makeText(this, "Mật khẩu mới không quá 30 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(currentPassword, newPass);
        });
    }

    private void changePassword(String currentPassword, String newPassword) {
        ChangePassword changePasswordUser = new ChangePassword();
        changePasswordUser.setUser_id(USER_ID);
        changePasswordUser.setCurrent_password(currentPassword);
        changePasswordUser.setNew_password(newPassword); // Giả sử bạn thêm setter này trong User
        Log.d(TAG, "🔥 Request body: { user_id: " + USER_ID + ", current_password: "
                + currentPassword + ", new_password: " + newPassword + " }");

        ApiService apiService = RetrofitClient.getClient();
        Call<ResponseBody> call = apiService.changePassword(changePasswordUser);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        setResult(RESULT_OK);
                        Toast.makeText(ChangePasswordActivity.this,
                                "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMessage = "Lỗi không xác định từ server";
                        if (response.errorBody() != null) {
                            String jsonResponse = response.errorBody().string();
                            Log.d(TAG, "🔥 Change password error response: " + jsonResponse);
                            JSONObject jsonResult = new JSONObject(jsonResponse);
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
                        }
                        Toast.makeText(ChangePasswordActivity.this, errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response: ", e);
                    Toast.makeText(ChangePasswordActivity.this, "Lỗi phản hồi từ server",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network error: ", t);
                Toast.makeText(ChangePasswordActivity.this, "Lỗi kết nối server: "
                        + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}