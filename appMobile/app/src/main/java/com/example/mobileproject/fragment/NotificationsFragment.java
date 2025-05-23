package com.example.mobileproject.fragment;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.mobileproject.model.NotificationCreate;
import com.example.mobileproject.model.NotificationModel;
import com.example.mobileproject.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private FloatingActionButton fabAddNotification;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo SessionManager
        sessionManager = SessionManager.getInstance(requireContext());

        // Khởi tạo các thành phần UI
        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        emptyText = view.findViewById(R.id.emptyText);
        progressBar = view.findViewById(R.id.progressBar);
        fabAddNotification = view.findViewById(R.id.fabAddNotification);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(getContext(), notifications);
        recyclerView.setAdapter(adapter);

        // Kiểm tra vai trò người dùng và hiển thị FAB tương ứng
        checkUserRole();

        // Set click listener cho FAB
        fabAddNotification.setOnClickListener(v -> showAddNotificationDialog());

        // Set click listener cho các thông báo
        adapter.setOnNotificationClickListener(notification -> {
            // Đánh dấu thông báo là đã đọc
            markNotificationAsRead(notification.getNotificationId());

            // Cập nhật UI
            notification.setIsRead(1);
            adapter.notifyDataSetChanged();

            // Hiển thị thông báo
            Toast.makeText(getContext(), "Đã đọc: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
        });

        // Tải thông báo
        fetchNotifications();
    }

    private void checkUserRole() {
        // Kiểm tra vai trò người dùng từ SharedPreferences
        String userRole = sessionManager.getUserRole();

        // Hiển thị FAB nếu vai trò là "instructor"
        if ("instructor".equalsIgnoreCase(userRole)) {
            fabAddNotification.setVisibility(View.VISIBLE);
        } else {
            fabAddNotification.setVisibility(View.GONE);
        }
    }

    private void showAddNotificationDialog() {
        // Tạo layout cho dialog
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_notification, null);

        EditText etTitle = dialogView.findViewById(R.id.etNotificationTitle);
        EditText etMessage = dialogView.findViewById(R.id.etNotificationMessage);
        EditText etImageUrl = dialogView.findViewById(R.id.etNotificationImageUrl);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSend = dialogView.findViewById(R.id.btnSend);

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Thiết lập theme và kiểu hiển thị
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Hiển thị dialog
        dialog.show();

        // Set listener cho nút Hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Set listener cho nút Gửi
        btnSend.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String message = etMessage.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị trạng thái đang gửi
            Toast.makeText(getContext(), "Đang gửi thông báo...", Toast.LENGTH_SHORT).show();

            // Gửi thông báo
            sendNotificationToAllUsers(title, message, imageUrl);

            // Đóng dialog
            dialog.dismiss();
        });
    }

    private void sendNotificationToAllUsers(String title, String message, String imageUrl) {
        // Tạo đối tượng NotificationCreate
        NotificationCreate notification = new NotificationCreate();
        notification.setTitle(title);
        notification.setMessage(message);

        if (!imageUrl.isEmpty()) {
            notification.setImageUrl(imageUrl);
        }

        // Gọi API để gửi thông báo
        ApiService apiService = RetrofitClient.getClient();
        Call<NotificationModel> call = apiService.createNotificationForUsers(notification);

        call.enqueue(new Callback<NotificationModel>() {
            @Override
            public void onResponse(Call<NotificationModel> call, Response<NotificationModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Đã gửi thông báo thành công", Toast.LENGTH_SHORT).show();

                    // Tải lại danh sách thông báo
                    fetchNotifications();
                } else {
                    Toast.makeText(getContext(), "Không thể gửi thông báo", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error sending notification: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NotificationModel> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Connection error: " + t.getMessage());
            }
        });
    }

    private void fetchNotifications() {
        showLoading();

        // Lấy ID người dùng từ SessionManager
        Integer userId = sessionManager.getUserId();
        if (userId == null || userId == -1) {
            showError("Không thể lấy thông tin người dùng");
            return;
        }

        // Gọi API
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
                    // Nếu API gặp lỗi, tải dữ liệu mẫu
                    loadMockNotifications();
                }
            }

            @Override
            public void onFailure(Call<List<NotificationModel>> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                showError("Lỗi kết nối: " + t.getMessage());

                // Nếu API gặp lỗi, tải dữ liệu mẫu
                loadMockNotifications();
            }
        });
    }

    private void markNotificationAsRead(int notificationId) {
        Integer userId = sessionManager.getUserId();
        if (userId == null || userId == -1) return;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadMockNotifications() {
        notifications.clear();

        // Tạo thông báo mẫu
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

    @Override
    public void onResume() {
        super.onResume();
        // Kiểm tra lại vai trò người dùng khi fragment được hiển thị lại
        checkUserRole();
        // Tải lại danh sách thông báo
        fetchNotifications();
    }
}