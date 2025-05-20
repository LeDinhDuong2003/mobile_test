package com.example.mobileproject.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobileproject.R;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

public class CodeFragment extends Fragment {
    private static final String TAG = "🔥 quan 🔥";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_CODE = "code";

    private EditText edtCode1, edtCode2, edtCode3, edtCode4;
    private Button btnContinue;
    private TextView tvResendCode, tvCountdown;
    private String phone;
    private String correctCode;
    private CountDownTimer countDownTimer;
    private CodeFragmentListener listener;

    private static final String API_KEY = "ae570ffe";
    private static final String API_SECRET = "jqBpx3nYpjc6DerO";

    public interface CodeFragmentListener {
        void onCodeVerified(String phone);
    }

    public CodeFragment() {
    }

    public static CodeFragment newInstance(String phone, String code) {
        CodeFragment fragment = new CodeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_CODE, code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phone = getArguments().getString(ARG_PHONE);
            correctCode = getArguments().getString(ARG_CODE);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CodeFragmentListener) {
            listener = (CodeFragmentListener) context;
        } else {
            throw new RuntimeException(context + " must implement CodeFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nhapcode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtCode1 = view.findViewById(R.id.code_s1);
        edtCode2 = view.findViewById(R.id.code_s2);
        edtCode3 = view.findViewById(R.id.code_s3);
        edtCode4 = view.findViewById(R.id.code_s4);
        btnContinue = view.findViewById(R.id.register_btn_createaccount);
        tvResendCode = view.findViewById(R.id.code_resendcode);
        tvCountdown = view.findViewById(R.id.countdown_timer);

        setupTextWatchers();
        startCountdown();

        btnContinue.setOnClickListener(v -> {
            String code = edtCode1.getText().toString() + edtCode2.getText().toString() +
                    edtCode3.getText().toString() + edtCode4.getText().toString();
            if (code.length() != 4) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ 4 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }
            if (code.equals(correctCode)) {
                Toast.makeText(getContext(), "Xác nhận mã thành công", Toast.LENGTH_SHORT).show();
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                if (listener != null) {
                    listener.onCodeVerified(phone);
                }
            } else {
                Toast.makeText(getContext(), "Mã xác nhận không đúng", Toast.LENGTH_SHORT).show();
            }
        });
        tvResendCode.setOnClickListener(v -> resendCode());
    }

    private void setupTextWatchers() {
        edtCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    edtCode2.requestFocus();
                }
            }
        });
        edtCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    edtCode3.requestFocus();
                } else if (s.length() == 0) {
                    edtCode1.requestFocus();
                }
            }
        });
        edtCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    edtCode4.requestFocus();
                } else if (s.length() == 0) {
                    edtCode2.requestFocus();
                }
            }
        });
        edtCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    edtCode3.requestFocus();
                }
            }
        });
    }

    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(10 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                tvCountdown.setText(String.format("Mã hết hạn sau: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Mã đã hết hạn");
                tvResendCode.setEnabled(true);
            }
        }.start();
    }

    private void resendCode() {
        correctCode = String.format("%04d", new Random().nextInt(10000));
        Log.d(TAG, "🔥 Generated new code: " + correctCode);
        sendSms(phone, correctCode);
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
                        Toast.makeText(getContext(), "Đã gửi lại mã xác nhận, bạn hãy chú ý điện thoại của mình", Toast.LENGTH_SHORT).show();
                        proceedAfterResend(code);
                    } else {
                        Log.e(TAG, "🔥 SMS failed: " + errorText);
                        Toast.makeText(getContext(), "Không thể gửi SMS: " + errorText + ". Mã xác nhận: " + code, Toast.LENGTH_LONG).show();
                        proceedAfterResend(code);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "🔥 SMS failed: " + e.getMessage(), e);
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Không thể gửi SMS: " + e.getMessage() + ". Mã xác nhận: " + code, Toast.LENGTH_LONG).show();
                    proceedAfterResend(code);
                });
            }
        }).start();
    }

    private void proceedAfterResend(String newCode) {
        correctCode = newCode;
        startCountdown();
        edtCode1.setText("");
        edtCode2.setText("");
        edtCode3.setText("");
        edtCode4.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}