package com.example.mobileproject.model;

import java.time.LocalDateTime;

import com.google.gson.annotations.SerializedName;

public class FCMTokenResponse {
    @SerializedName("token_id")
    private int tokenId;

    @SerializedName("user_id")
    private int userId;

    private String token;

    @SerializedName("device_type")
    private String deviceType;

    @SerializedName("last_updated")
    private LocalDateTime lastUpdated;

    // Getters and setters
    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}