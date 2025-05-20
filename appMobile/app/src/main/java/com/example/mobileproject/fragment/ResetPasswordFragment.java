package com.example.mobileproject.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobileproject.LoginActivity;
import com.example.mobileproject.R;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.User;

import org.json.JSONObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordFragment extends Fragment {
    private static final String TAG = "🔥 quan 🔥";
    private static final String ARG_PHONE = "phone";

    private EditText edtNewPassword, edtConfirmPassword;
    private Button btnContinue;
    private String phone;
    private ResetPasswordFragmentListener listener;

    public interface ResetPasswordFragmentListener {
        void onPasswordReset();
    }

    public ResetPasswordFragment() {
    }

    public static ResetPasswordFragment newInstance(String phone) {
        ResetPasswordFragment fragment = new ResetPasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phone = getArguments().getString(ARG_PHONE);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ResetPasswordFragmentListener) {
            listener = (ResetPasswordFragmentListener) context;
        } else {
            throw new RuntimeException(context + " must implement ResetPasswordFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.laylaimatkhau, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtNewPassword = view.findViewById(R.id.getpassword_newpassword);
        edtConfirmPassword = view.findViewById(R.id.getpassword_confirmpassword);
        btnContinue = view.findViewById(R.id.getpassword_btn_continue);

        btnContinue.setOnClickListener(v -> {
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩum mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() > 30) {
                Toast.makeText(getContext(), "Mật khẩu mới không quá 30 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            User resetUser = new User();
            resetUser.setPhone(phone);
            resetUser.setPassword(newPassword);
            Log.d(TAG, "🔥 Request body: { phone: "+phone+", new_password: "+newPassword+" }");

            ApiService apiService = RetrofitClient.getClient();
            Call<ResponseBody> call = apiService.resetPassword(resetUser);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        if (listener != null) {
                            listener.onPasswordReset();
                        }
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
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Network error: ", t);
                    Toast.makeText(getContext(), "Lỗi kết nối: " +
                            t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
