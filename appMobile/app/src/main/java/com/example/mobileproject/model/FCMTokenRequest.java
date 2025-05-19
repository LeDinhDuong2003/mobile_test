package com.example.mobileproject.model;

public class FCMTokenRequest {
    private String token;
    private String device_type;

    public FCMTokenRequest(String token) {
        this.token = token;
        this.device_type = "android";
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }
}