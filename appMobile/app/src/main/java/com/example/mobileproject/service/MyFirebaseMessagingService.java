package com.example.mobileproject.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.mobileproject.MainActivityHomePage;
import com.example.mobileproject.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_Service";
    private static final String CHANNEL_ID = "online_courses_channel";
    private static final String CHANNEL_NAME = "Online Courses Notifications";
    private static final String CHANNEL_DESC = "Thông báo từ ứng dụng Online Courses";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Kiểm tra xem thông báo có dữ liệu không
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Kiểm tra xem thông báo có nội dung hiển thị không
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            sendNotification(title, body, remoteMessage.getData());
        }
    }

    /**
     * Xử lý thông báo có dữ liệu
     */
    private void handleDataMessage(Map<String, String> data) {
        try {
            String title = data.get("title");
            String message = data.get("message");
            String imageUrl = data.get("image_url");
            String notificationType = data.get("type");
            String notificationId = data.get("notification_id");

            // Lưu thông báo vào cơ sở dữ liệu local (SQLite hoặc Room)
            saveNotificationToLocal(title, message, imageUrl, notificationType, notificationId);

            // Hiển thị thông báo
            if (title != null && message != null) {
                sendNotification(title, message, data);
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Lưu thông báo vào cơ sở dữ liệu local
     */
    private void saveNotificationToLocal(String title, String message, String imageUrl,
                                         String notificationType, String notificationId) {
        // Ở đây bạn sẽ lưu thông báo vào cơ sở dữ liệu local
        // Để đơn giản, tôi sẽ chỉ log lại thông tin
        Log.d(TAG, "Save to local DB: " + title + " - " + message);

        // Trong ứng dụng thực tế, bạn sẽ sử dụng Room Database hoặc SQLite để lưu trữ
    }

    /**
     * Hiển thị thông báo
     */
    private void sendNotification(String title, String messageBody, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivityHomePage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Truyền dữ liệu từ thông báo vào intent
        if (data != null) {
            for (String key : data.keySet()) {
                intent.putExtra(key, data.get(key));
            }
        }

        // Đặt action cho intent dựa vào loại thông báo
        String notificationType = data != null ? data.get("type") : null;
        if (notificationType != null) {
            intent.setAction(notificationType);
        }

        // Tạo PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Âm thanh thông báo
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Tạo thông báo
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notifications)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo notification channel cho Android O trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
        }

        // Hiển thị thông báo
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Nhận token FCM mới khi token được cấp hoặc cập nhật
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // Gửi token FCM mới lên server
        sendRegistrationToServer(token);
    }

    /**
     * Gửi token FCM đến server của ứng dụng
     */
    private void sendRegistrationToServer(String token) {
        // Implement code để gửi token lên server của bạn
        // Để đơn giản, tôi sẽ chỉ log lại token
        Log.d(TAG, "sendRegistrationTokenToServer: " + token);

        // Trong ứng dụng thực tế, bạn sẽ gửi token này đến server thông qua API
    }
}