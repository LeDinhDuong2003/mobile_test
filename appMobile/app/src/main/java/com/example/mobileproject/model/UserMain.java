package com.example.mobileproject.model;

public class UserMain {
    private String id;
    private String name;
    private String email;
    private String profileImageUrl;

    // Constructor mặc định
    public UserMain() {
    }

    // Constructor đầy đủ tham số
    public UserMain(String id, String name, String email, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters và setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // Phương thức lấy lời chào với tên người dùng
    public String getGreeting() {
        return "Hi, " + name + " 👋";
    }
}