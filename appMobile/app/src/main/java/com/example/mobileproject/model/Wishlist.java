package com.example.mobileproject.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Wishlist implements Serializable {
    private Integer wishlistId;
    private Integer userId;
    private Integer courseId;
    private LocalDateTime createdAt;
    private User user;
    private Course course;

    // Constructor
    public Wishlist() {
    }

    // Getters and Setters
    public Integer getWishlistId() { return wishlistId; }
    public void setWishlistId(Integer wishlistId) { this.wishlistId = wishlistId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}
