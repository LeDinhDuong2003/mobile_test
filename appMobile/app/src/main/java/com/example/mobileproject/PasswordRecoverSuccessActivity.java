package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class PasswordRecoverSuccessActivity extends AppCompatActivity {
    private Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.laylaimatkhauthanhcong);

        // Liên kết với layout
        btnExit = findViewById(R.id.passwordrecoversuccess_exit);

        // Xử lý nút Exit
        btnExit.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}