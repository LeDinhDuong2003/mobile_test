package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.fragment.FavoriteFragment;
import com.example.mobileproject.fragment.HomeFragment;
import com.example.mobileproject.fragment.CoursesFragment;
import com.example.mobileproject.fragment.NotificationsFragment;
import com.example.mobileproject.fragment.ProfileFragment;
import com.example.mobileproject.model.FCMTokenRequest;
import com.example.mobileproject.model.FCMTokenResponse;
import com.example.mobileproject.model.UserMain;
import com.example.mobileproject.repository.DataRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityHomePage extends AppCompatActivity {

    private TextView tvGreeting;
    private CardView btnLeftAction;
    private ImageView leftActionIcon;
    private boolean isBackButton = false;
    private UserMain currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        getFCMToken();

        // Kiểm tra xem ứng dụng được mở từ thông báo FCM hay không
        checkNotificationIntent(getIntent());

        // Lấy thông tin người dùng
        currentUser = DataRepository.getCurrentUser();
        // Khởi tạo UI
        initUI();
        // Hiển thị fragment Home mặc định
        loadFragment(new HomeFragment());
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Kiểm tra xem ứng dụng đang chạy và được mở từ thông báo FCM hay không
        checkNotificationIntent(intent);
    }

    private void checkNotificationIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            // Kiểm tra xem có dữ liệu từ FCM không
            if (intent.hasExtra("notification_id")) {
                String notificationId = intent.getStringExtra("notification_id");
                String type = intent.getStringExtra("type");

                // Chuyển đến màn hình thông báo
                navigateToNotifications();

                // Đánh dấu thông báo đã đọc nếu có notification_id
                if (notificationId != null) {
                    markNotificationAsRead(Integer.parseInt(notificationId));
                }
            }
        }
    }
    private void navigateToNotifications() {
        // Chuyển đến fragment thông báo
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_notifications);
        }
    }

    private void markNotificationAsRead(int notificationId) {
        // Implement code để đánh dấu thông báo đã đọc
        Integer userId = getUserId();
        if (userId != null) {
            ApiService apiService = RetrofitClient.getClient();
            Call<Void> call = apiService.markNotificationAsRead(userId, notificationId);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("Notification", "Marked as read: " + notificationId);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("Notification", "Failed to mark as read: " + t.getMessage());
                }
            });
        }
    }

    private Integer getUserId() {
        // Implement code để lấy user ID từ SharedPreferences hoặc UserManager
        // Trong ví dụ này, trả về 1 cho mục đích minh họa
        return 1;
    }

    private void initUI() {
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLeftAction = findViewById(R.id.btnLeftAction);
        leftActionIcon = findViewById(R.id.leftActionIcon);

        if (tvGreeting != null) {
            tvGreeting.setText(currentUser.getGreeting());
        }

        // Thiết lập Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView != null) {
            // Đảm bảo chọn item Home mặc định
            bottomNavigationView.setSelectedItemId(R.id.nav_home);

            // Thiết lập listener
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    setMenuButton(); // Chuyển về Menu khi về Home
                }
                else if (itemId == R.id.nav_favorite) {
                    selectedFragment = new FavoriteFragment();
                    setMenuButton(); // Reset về Menu cho các tabs khác
                }
                else if (itemId == R.id.nav_notifications) {
                    selectedFragment = new NotificationsFragment();
                    setMenuButton(); // Reset về Menu cho các tabs khác
                }
                else if (itemId == R.id.nav_courses) {
                    selectedFragment = new CoursesFragment();
                    setMenuButton(); // Reset về Menu cho các tabs khác
                }
                else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    setMenuButton(); // Reset về Menu cho các tabs khác
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }

                return false;
            });
        }

        // Thiết lập sự kiện cho nút ở góc trái (Menu hoặc Back)
        if (btnLeftAction != null) {
            btnLeftAction.setOnClickListener(v -> {
                if (isBackButton) {
                    // Nếu là nút Back, quay lại fragment trước đó
                    onBackPressed();
                } else {
                    // Nếu là nút Menu, mở menu
                    Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Thiết lập sự kiện cho nút Cart
        View btnCart = findViewById(R.id.btnCart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                // Xử lý khi nhấn nút Cart
                Toast.makeText(this, "Cart clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Phương thức để chuyển sang nút Menu
    public void setMenuButton() {
        if (leftActionIcon != null) {
            leftActionIcon.setImageResource(R.drawable.ic_menu);
            isBackButton = false;
        }
    }

    // Phương thức để chuyển sang nút Back
    public void setBackButton() {
        if (leftActionIcon != null) {
            leftActionIcon.setImageResource(R.drawable.ic_back);
            isBackButton = true;
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM_Token", "Lấy FCM token thất bại", task.getException());
                        return;
                    }

                    // Lấy token mới
                    String token = task.getResult();

                    // Log token để debug
                    Log.d("FCM_Token", "FCM Token: " + token);

                    // Gửi token lên server
                    sendTokenToServer(token);
                });
    }

    private void sendTokenToServer(String token) {
        // Lấy user_id từ SharedPreferences hoặc cách khác
        Integer userId = getUserId(); // Giả sử phương thức này trả về user_id hiện tại
        if (userId == null) {
            Log.e("FCM", "Không thể lấy user_id");
            return;
        }

        ApiService apiService = RetrofitClient.getClient();
        FCMTokenRequest request = new FCMTokenRequest(token);

        Call<FCMTokenResponse> call = apiService.updateFCMToken(userId, request);
        call.enqueue(new Callback<FCMTokenResponse>() {
            @Override
            public void onResponse(Call<FCMTokenResponse> call, Response<FCMTokenResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM", "Token đã được gửi thành công đến server: " + token);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("FCM", "Lỗi khi gửi token: " + response.code() + " - " + errorBody);
                    } catch (IOException e) {
                        Log.e("FCM", "Lỗi đọc error body: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<FCMTokenResponse> call, Throwable t) {
                Log.e("FCM", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

}