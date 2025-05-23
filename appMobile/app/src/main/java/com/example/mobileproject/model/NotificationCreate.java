package com.example.mobileproject.model;

import com.google.gson.annotations.SerializedName;

public class NotificationCreate {
    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("image_url")
    private String imageUrl;

    // Constructor
    public NotificationCreate() {
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}