package com.example.mobileproject.fragment;

import android.content.Context;
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

import com.example.mobileproject.R;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.User;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneFragment extends Fragment {
    private static final String TAG = "🔥 quan 🔥";
    private EditText edtPhone;
    private Button btnContinue;
    private PhoneFragmentListener listener;

    private static final String API_KEY = "ae570ffe";
    private static final String API_SECRET = "jqBpx3nYpjc6DerO";

    public interface PhoneFragmentListener {
        void onPhoneVerified(String phone, String code);
    }

    public PhoneFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PhoneFragmentListener) {
            listener = (PhoneFragmentListener) context;
        } else {
            throw new RuntimeException(context + " must implement PhoneFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quenmatkhau, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtPhone = view.findViewById(R.id.register_phone);
        btnContinue = view.findViewById(R.id.register_btn_createaccount);

        btnContinue.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }
            checkPhoneNumber(phone);
        });
    }

    private void checkPhoneNumber(String phone) {
        User checkUser = new User();
        checkUser.setPhone(phone);
        Log.d(TAG, "🔥 Request body: { phone: " + phone + " }");

        ApiService apiService = RetrofitClient.getClient();
        Call<ResponseBody> call = apiService.checkPhone(checkUser);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String code = String.format("%04d", new Random().nextInt(10000));
                    Log.d(TAG, "🔥 Generated code: " + code);
                    sendSms(phone, code);
                } else {
                    String errorMessage = "Lỗi kiểm tra số điện thoại";
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorJson = new JSONObject(response.errorBody().string());
                            errorMessage = errorJson.optString("detail", errorMessage);
                            Log.e(TAG, "🔥 Server error response: " + errorJson.toString());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response: ", e);
                    }
                    Toast.makeText(getContext(),
                            errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network error: ", t);
                Toast.makeText(getContext(), "Lỗi kết nối: "
                        + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSms(String phone, String code) {
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

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if ("0".equals(status)) {
                        Log.d(TAG, "🔥 SMS sent successfully");
                        Toast.makeText(getContext(), "Đã gửi mã xác nhận, bạn hãy chú ý điện thoại của mình", Toast.LENGTH_SHORT).show();
                        proceedToEnterCode(phone, code);
                    } else {
                        Log.e(TAG, "🔥 SMS failed: " + errorText);
                        Toast.makeText(getContext(), "Không thể gửi SMS: " + errorText + ". Mã xác nhận: " + code, Toast.LENGTH_LONG).show();
                        proceedToEnterCode(phone, code);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "🔥 SMS failed: " + e.getMessage(), e);
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Không thể gửi SMS: " + e.getMessage() + ". Mã xác nhận: " + code, Toast.LENGTH_LONG).show();
                    proceedToEnterCode(phone, code);
                });
            }
        }).start();
    }

    private void proceedToEnterCode(String phone, String code) {
        if (listener != null) {
            listener.onPhoneVerified(phone, code);
        }
    }
}
