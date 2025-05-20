package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.mobileproject.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityHomePage extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView tvGreeting;
    private CardView btnLeftAction;
    private ImageView leftActionIcon;
    private boolean isBackButton = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SessionManager
        sessionManager = SessionManager.getInstance(this);

        FirebaseApp.initializeApp(this);
        getFCMToken();

        // Check if app was opened from FCM notification
        checkNotificationIntent(getIntent());

        // Initialize UI
        initUI();

        // Display default Home fragment
        loadFragment(new HomeFragment());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkNotificationIntent(intent);
    }

    private void checkNotificationIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            // Check if there's data from FCM
            if (intent.hasExtra("notification_id")) {
                String notificationId = intent.getStringExtra("notification_id");
                String type = intent.getStringExtra("type");

                // Navigate to notifications screen
                navigateToNotifications();

                // Mark notification as read if notification_id exists
                if (notificationId != null) {
                    markNotificationAsRead(Integer.parseInt(notificationId));
                }
            }
        }
    }

    private void navigateToNotifications() {
        // Navigate to notifications fragment
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_notifications);
        }
    }

    private void markNotificationAsRead(int notificationId) {
        // Implementation to mark notification as read
        Integer userId = getUserId();
        if (userId != null) {
            ApiService apiService = RetrofitClient.getClient();
            Call<Void> call = apiService.markNotificationAsRead(userId, notificationId);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Marked as read: " + notificationId);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Failed to mark as read: " + t.getMessage());
                }
            });
        }
    }

    private Integer getUserId() {
        // Get user ID from SessionManager
        int userId = sessionManager.getUserId();
        return userId != -1 ? userId : null;
    }

    private void initUI() {
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLeftAction = findViewById(R.id.btnLeftAction);
        leftActionIcon = findViewById(R.id.leftActionIcon);

        if (tvGreeting != null) {
            tvGreeting.setText("Hi, " + sessionManager.getUserName() + " 👋");
        }

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView != null) {
            // Ensure Home item is selected by default
            bottomNavigationView.setSelectedItemId(R.id.nav_home);

            // Set up listener
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    setMenuButton(); // Switch to Menu when going Home
                }
                else if (itemId == R.id.nav_favorite) {
                    selectedFragment = new FavoriteFragment();
                    setMenuButton(); // Reset to Menu for other tabs
                }
                else if (itemId == R.id.nav_notifications) {
                    selectedFragment = new NotificationsFragment();
                    setMenuButton(); // Reset to Menu for other tabs
                }
                else if (itemId == R.id.nav_courses) {
                    selectedFragment = new CoursesFragment();
                    setMenuButton(); // Reset to Menu for other tabs
                }
                else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    setMenuButton(); // Reset to Menu for other tabs
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }

                return false;
            });
        }

        // Set up event for left corner button (Menu or Back)
        if (btnLeftAction != null) {
            btnLeftAction.setOnClickListener(v -> {
                if (isBackButton) {
                    // If it's Back button, go back to previous fragment
                    onBackPressed();
                } else {
                    // If it's Menu button, open menu
                    Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Set up event for Cart button
        View btnCart = findViewById(R.id.btnCart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                // Handle Cart button click
                Toast.makeText(this, "Cart clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Method to switch to Menu button
    public void setMenuButton() {
        if (leftActionIcon != null) {
            leftActionIcon.setImageResource(R.drawable.ic_menu);
            isBackButton = false;
        }
    }

    // Method to switch to Back button
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
                        Log.w(TAG, "Failed to get FCM token", task.getException());
                        return;
                    }

                    // Get new token
                    String token = task.getResult();

                    // Log token for debugging
                    Log.d(TAG, "FCM Token: " + token);

                    // Send token to server
                    sendTokenToServer(token);
                });
    }

    private void sendTokenToServer(String token) {
        // Get user_id from SessionManager
        Integer userId = getUserId();
        if (userId == null) {
            Log.e(TAG, "Cannot get user_id");
            return;
        }

        ApiService apiService = RetrofitClient.getClient();
        FCMTokenRequest request = new FCMTokenRequest(token);

        Call<FCMTokenResponse> call = apiService.updateFCMToken(userId, request);
        call.enqueue(new Callback<FCMTokenResponse>() {
            @Override
            public void onResponse(Call<FCMTokenResponse> call, Response<FCMTokenResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Token successfully sent to server: " + token);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error sending token: " + response.code() + " - " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<FCMTokenResponse> call, Throwable t) {
                Log.e(TAG, "Connection error: " + t.getMessage());
            }
        });
    }
}