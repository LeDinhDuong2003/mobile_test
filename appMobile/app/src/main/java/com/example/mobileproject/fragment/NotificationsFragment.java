package com.example.mobileproject.fragment;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.adapter.NotificationAdapter;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.NotificationModel;
import com.example.mobileproject.repository.DataRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationModel> notifications = new ArrayList<>();
    private TextView emptyText;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        emptyText = view.findViewById(R.id.emptyText);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(getContext(), notifications);
        recyclerView.setAdapter(adapter);

        // Set click listener
        adapter.setOnNotificationClickListener(notification -> {
            // Mark as read
            markNotificationAsRead(notification.getNotificationId());

            // Update UI
            notification.setIsRead(1);
            adapter.notifyDataSetChanged();

            // Show message
            Toast.makeText(getContext(), "Đã đọc: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
        });

        // Fetch notifications
        fetchNotifications();
    }

    private void fetchNotifications() {
        showLoading();

        // Get current user ID
        Integer userId = getUserId();
        if (userId == null) {
            showError("Không thể lấy thông tin người dùng");
            return;
        }

        // Call API
        ApiService apiService = RetrofitClient.getClient();
        Call<List<NotificationModel>> call = apiService.getUserNotifications(userId);
        call.enqueue(new Callback<List<NotificationModel>>() {
            @Override
            public void onResponse(Call<List<NotificationModel>> call, Response<List<NotificationModel>> response) {
                hideLoading();

                if (response.isSuccessful() && response.body() != null) {
                    notifications.clear();
                    notifications.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (notifications.isEmpty()) {
                        showEmpty();
                    } else {
                        showContent();
                    }

                    Log.d(TAG, "Loaded " + notifications.size() + " notifications");
                } else {
                    Log.e(TAG, "Error fetching notifications: " + response.code());
                    // If API fails, load mock data
                    loadMockNotifications();
                }
            }

            @Override
            public void onFailure(Call<List<NotificationModel>> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                showError("Lỗi kết nối: " + t.getMessage());

                // If API fails, load mock data
                loadMockNotifications();
            }
        });
    }

    private void markNotificationAsRead(int notificationId) {
        Integer userId = getUserId();
        if (userId == null) return;

        ApiService apiService = RetrofitClient.getClient();
        Call<Void> call = apiService.markNotificationAsRead(userId, notificationId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Notification marked as read: " + notificationId);
                } else {
                    Log.e(TAG, "Failed to mark notification as read: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error when marking notification as read: " + t.getMessage(), t);
            }
        });
    }

    private Integer getUserId() {
        // In a real app, get the user ID from SharedPreferences or a UserManager
        try {
            return 1; // For testing purposes
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID: " + e.getMessage(), e);
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadMockNotifications() {
        notifications.clear();

        // Add mock notifications
        NotificationModel notification1 = new NotificationModel();
        notification1.setNotificationId(1);
        notification1.setTitle("Khóa học mới");
        notification1.setMessage("Chúng tôi vừa ra mắt khóa học mới về Android Development");
        notification1.setIsRead(0);
        notification1.setCreatedAt(LocalDateTime.now().minusHours(2));

        NotificationModel notification2 = new NotificationModel();
        notification2.setNotificationId(2);
        notification2.setTitle("Ưu đãi đặc biệt");
        notification2.setMessage("Giảm giá 50% cho tất cả các khóa học từ ngày 20/5 đến 30/5");
        notification2.setIsRead(1);
        notification2.setCreatedAt(LocalDateTime.now().minusDays(1));

        NotificationModel notification3 = new NotificationModel();
        notification3.setNotificationId(3);
        notification3.setTitle("Hoàn thành bài học");
        notification3.setMessage("Bạn đã hoàn thành 80% khóa học Introduction to Android");
        notification3.setIsRead(0);
        notification3.setCreatedAt(LocalDateTime.now().minusHours(12));

        notifications.add(notification1);
        notifications.add(notification2);
        notifications.add(notification3);

        adapter.notifyDataSetChanged();

        if (notifications.isEmpty()) {
            showEmpty();
        } else {
            showContent();
        }
    }

    private void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyText != null) emptyText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void showContent() {
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        if (emptyText != null) emptyText.setVisibility(View.GONE);
    }

    private void showEmpty() {
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyText != null) emptyText.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        showEmpty();
    }
}