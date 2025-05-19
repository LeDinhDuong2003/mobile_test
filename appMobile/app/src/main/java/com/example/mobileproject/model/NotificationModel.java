package com.example.mobileproject.model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class NotificationModel implements Serializable {
    @SerializedName("notification_id")
    private Integer notificationId;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("is_read")
    private Integer isRead;

    @SerializedName("created_at")
    private LocalDateTime createdAt;

    @SerializedName("image_url")
    private String imageUrl;

    // Constructor
    public NotificationModel() {
    }

    // Getters and Setters
    public Integer getNotificationId() { return notificationId; }
    public void setNotificationId(Integer notificationId) { this.notificationId = notificationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Helper method to get formatted time
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getTimeAgo() {
        if (createdAt == null) return "Unknown time";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);

        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (minutes < 1440) { // Less than a day
            long hours = minutes / 60;
            return hours + " giờ trước";
        } else if (minutes < 10080) { // Less than a week
            long days = minutes / 1440;
            return days + " ngày trước";
        } else {
            return createdAt.getDayOfMonth() + "/" + createdAt.getMonthValue() + "/" + createdAt.getYear();
        }
    }

    // Helper method to check if notification is read
    public boolean isRead() {
        return isRead != null && isRead == 1;
    }
}