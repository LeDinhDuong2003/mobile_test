package com.example.mobileproject;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class InformationActivity extends AppCompatActivity {
    private static final String TAG = "🔥 quan 🔥";
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int PREVIEW_REQUEST_CODE = 101;
    private static final int CHANGE_PROFILE_REQUEST_CODE = 102;
    private ImageView profileAvatar, profileBtnChangeAvatar, profileBtnEdit;
    private TextView profileName;
    private EditText profileEmail, profilePassword, profilePhone;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hoso);

        profileAvatar = findViewById(R.id.profile_avatar);
        profileBtnChangeAvatar = findViewById(R.id.profile_btnchangeavatar);
        profileBtnEdit = findViewById(R.id.profile_btnedit);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        profilePassword = findViewById(R.id.profile_password);
        profilePhone = findViewById(R.id.profile_phone);

        profileName.setText("Cô Văn Anh");
        profileEmail.setText("btlcovananh@gmail.com");
        profilePhone.setText("0864365278");
        profilePassword.setText(".................");

        // Load initial avatar from a hardcoded URL
        loadInitialAvatar();

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        Intent previewIntent = new Intent(this, PreviewActivity.class);
                        previewIntent.putExtra("image_uri", selectedImageUri.toString());
                        startActivityForResult(previewIntent, PREVIEW_REQUEST_CODE);
                    }
                });

        profileBtnChangeAvatar.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT < 29 &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                openImagePicker();
            }
        });

        profileBtnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangeProfileActivity.class);
            intent.putExtra("name", profileName.getText().toString());
            intent.putExtra("email", profileEmail.getText().toString());
            intent.putExtra("phone", profilePhone.getText().toString());
            startActivityForResult(intent, CHANGE_PROFILE_REQUEST_CODE);
        });
    }

    private void loadInitialAvatar() {
        String initialAvatarUrl = "https://ui-avatars.com/api/?name=A%2B&background=2196F3&color=fff&size=150";
        new Thread(() -> {
            try {
                Bitmap bitmap = loadBitmapFromUrl(initialAvatarUrl);
                runOnUiThread(() -> profileAvatar.setImageBitmap(bitmap));
            } catch (Exception e) {
                Log.e(TAG, "🔥 Load initial avatar error: ", e);
                runOnUiThread(() -> {
                    profileAvatar.setImageResource(R.drawable.img);
                    Toast.makeText(this, "Không thể tải ảnh đại diện", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Cần quyền truy cập thư viện để chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PREVIEW_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String newAvatarUrl = data.getStringExtra("avatar_url");
            if (newAvatarUrl != null) {
                new Thread(() -> {
                    try {
                        Bitmap bitmap = loadBitmapFromUrl(newAvatarUrl);
                        runOnUiThread(() -> {
                            profileAvatar.setImageBitmap(bitmap);
                            selectedImageUri = null;
                            Toast.makeText(this, "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "🔥 Load new avatar error: ", e);
                        runOnUiThread(() -> Toast.makeText(this, "Lỗi tải ảnh mới", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else {
                selectedImageUri = null;
            }
        } else if (requestCode == CHANGE_PROFILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String updatedName = data.getStringExtra("name");
            String updatedEmail = data.getStringExtra("email");
            String updatedPhone = data.getStringExtra("phone");
            profileName.setText(updatedName);
            profileEmail.setText(updatedEmail);
            profilePhone.setText(updatedPhone);
        }
    }

    private Bitmap loadBitmapFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoInput(true);
        conn.connect();
        InputStream inputStream = conn.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        conn.disconnect();
        return bitmap;
    }
}