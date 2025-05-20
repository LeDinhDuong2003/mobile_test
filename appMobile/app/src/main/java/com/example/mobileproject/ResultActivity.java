package com.example.mobileproject;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ResultActivity extends AppCompatActivity {

    private TextView titleText, scoreText, messageText;
    private ImageButton btnBack;
    private static final String TAG = "🔥 quan 🔥";
    private int USER_ID;
    private final Handler handler = new Handler();
    private int dotCount = 1;
    private Runnable dotRunnable;
    private SharedPreferences sharedPreferences;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ketquaquiz);
        sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        USER_ID = sharedPreferences.getInt("user_id", 1);
        titleText = findViewById(R.id.titleText);
        scoreText = findViewById(R.id.scoreText);
        messageText = findViewById(R.id.messageText);
        btnBack = findViewById(R.id.backButton);
        mediaPlayer = MediaPlayer.create(this, R.raw.result);
        titleText.setText("Hoàn Thành");
        scoreText.setText("");
        messageText.setText("Đang xử lý kết quả của bạn.");

        startDotAnimation();

        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 0);
        int questionId = getIntent().getIntExtra("question_id", -1);

        if (questionId == -1) {
            Toast.makeText(this, "Không có câu hỏi để lưu kết quả", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        saveQuizResult(USER_ID, questionId, score, total);

        btnBack.setOnClickListener(v->{
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            finish();
        });
    }

    private void startDotAnimation() {
        dotRunnable = new Runnable() {
            @Override
            public void run() {
                StringBuilder dots = new StringBuilder("Đang xử lý kết quả của bạn");
                for (int i = 0; i < dotCount; i++) {
                    dots.append(".");
                }
                messageText.setText(dots.toString());
                dotCount = (dotCount % 5) + 1; // 1 -> 5, lặp lại
                handler.postDelayed(this, 500); // Cập nhật mỗi 0.5s
            }
        };
        handler.post(dotRunnable);
    }

    private void saveQuizResult(int userId, int questionId, int score, int total) {
        new Thread(() -> {
            try {
                String apiUrl = getString(R.string.base_url) + "/save-quiz-result";
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject json = new JSONObject();
                json.put("user_id", userId);
                json.put("question_id", questionId);
                json.put("score", String.format("%d/%d", score, total)); // Gửi dạng "9/10"

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
                Log.d(TAG, "🔥 Save quiz result response: " + jsonResponse);

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResult = new JSONObject(jsonResponse);
                        if (responseCode >= 200 && responseCode < 300) {
                            Log.e(TAG, "🔥Lưu kết quả thành công ");
                            scoreText.setText(String.format("Điểm: %d/%d", score, total));
                            scoreText.setVisibility(View.VISIBLE);
                            handler.removeCallbacks(dotRunnable);
                            messageText.setVisibility(View.GONE);
                            if (mediaPlayer != null) {
                                mediaPlayer.start();
                            }
                        } else {
                            String errorMessage = jsonResult.has("error") ?
                                    jsonResult.getString("error") :
                                    "Lỗi không xác định: " + jsonResponse;
                            Toast.makeText(this, "Lỗi lưu kết quả: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "🔥 JSON parse error: ", e);
                        Toast.makeText(this, "Lỗi phân tích dữ liệu: " + jsonResponse, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "🔥 Network error: ", e);
                runOnUiThread(() -> Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(dotRunnable); // Dừng animation
    }
}