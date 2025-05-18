package com.example.mobileproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PreviewActivity extends AppCompatActivity {
    private static final String TAG = "🔥 quan 🔥";
    private static final int USER_ID = 1;
    private Uri imageUri;
    private TextView loadingText;
    private Handler handler;
    private Runnable dotsRunnable;
    private boolean isUploading = false;
    private int dotCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xemtruocavatar);

        ImageView previewAvatar = findViewById(R.id.preview_avatar);
        Button backButton = findViewById(R.id.back_button);
        Button saveButton = findViewById(R.id.save_button);
        loadingText = findViewById(R.id.loading_text);

        handler = new Handler(Looper.getMainLooper());
        dotsRunnable = new Runnable() {
            @Override
            public void run() {
                if (isUploading) {
                    dotCount = (dotCount % 5) + 1; // Cycle from 1 to 5
                    StringBuilder dots = new StringBuilder("Vui lòng chờ");
                    for (int i = 0; i < dotCount; i++) {
                        dots.append(".");
                    }
                    loadingText.setText(dots.toString());
                    handler.postDelayed(this, 500); // Update every 500ms
                }
            }
        };

        String imageUriString = getIntent().getStringExtra("image_uri");
        if (imageUriString != null) {
            try {
                imageUri = Uri.parse(imageUriString);
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                previewAvatar.setImageBitmap(bitmap);
                inputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "🔥 Load preview image error: ", e);
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }

        backButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        saveButton.setOnClickListener(v -> {
            isUploading = true;
            loadingText.setVisibility(View.VISIBLE);
            handler.post(dotsRunnable); // Start dot animation
            uploadImageToApi(imageUri);
        });
    }

    private void stopLoadingAnimation() {
        isUploading = false;
        handler.removeCallbacks(dotsRunnable);
        loadingText.setVisibility(View.GONE);
    }

    private void uploadImageToApi(Uri imageUri) {
        new Thread(() -> {
            try {
                String apiUrl = getString(R.string.base_url) + "/upload-image";
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                String boundary = "----" + System.currentTimeMillis();
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream os = conn.getOutputStream()) {
                    String userIdField = "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"user_id\"\r\n\r\n" +
                            USER_ID + "\r\n";
                    os.write(userIdField.getBytes(StandardCharsets.UTF_8));

                    String fileField = "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"file\"; filename=\"user_" + USER_ID + ".jpg\"\r\n" +
                            "Content-Type: image/jpeg\r\n\r\n";
                    os.write(fileField.getBytes(StandardCharsets.UTF_8));

                    try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                    os.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
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
                Log.d(TAG, "🔥 Upload image response: " + jsonResponse);

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(jsonResponse);
                        if (responseCode >= 200 && responseCode < 300) {
                            String newAvatarUrl = json.getString("url");
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("avatar_url", newAvatarUrl);
                            setResult(RESULT_OK, resultIntent);
                            stopLoadingAnimation();
                            finish();
                        } else {
                            stopLoadingAnimation();
                            Toast.makeText(this, "Lỗi upload ảnh: " + json.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "🔥 JSON parse error: ", e);
                        stopLoadingAnimation();
                        Toast.makeText(this, "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "🔥 Upload error: ", e);
                runOnUiThread(() -> {
                    stopLoadingAnimation();
                    Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}