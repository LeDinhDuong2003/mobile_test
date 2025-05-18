package com.example.mobileproject.model;

import com.google.gson.annotations.SerializedName;

import org.w3c.dom.Comment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User implements Serializable {
    @SerializedName("user_id")
    private Integer userId;
    @SerializedName("username")
    private String username;
    @SerializedName("full_name")
    private String fullName;
    @SerializedName("password")
    private String password;
    @SerializedName("email")
    private String email;
    @SerializedName("phone")
    private String phone;
    @SerializedName("avatar_url")
    private String avatarUrl;
    @SerializedName("google_id")
    private String googleId;
    @SerializedName("role")
    private String role;
    @SerializedName("created_at")
    private LocalDateTime createdAt;
    private List<Wishlist> wishlists = new ArrayList<>();
    private List<Enrollment> enrollments = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
    private List<QuizResult> quizResults = new ArrayList<>();
    private List<Notification> notifications = new ArrayList<>();

    // Constructor
    public User() {
    }

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Wishlist> getWishlists() { return wishlists; }
    public void setWishlists(List<Wishlist> wishlists) { this.wishlists = wishlists; }
    public List<Enrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(List<Enrollment> enrollments) { this.enrollments = enrollments; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    public List<QuizResult> getQuizResults() { return quizResults; }
    public void setQuizResults(List<QuizResult> quizResults) { this.quizResults = quizResults; }
    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }

    public List<Course> getCourses() {
        return enrollments.stream()
                .map(Enrollment::getCourse)
                .collect(Collectors.toList());
    }

}