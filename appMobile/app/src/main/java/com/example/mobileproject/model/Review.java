package com.example.mobileproject.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Review implements Serializable {
    @SerializedName("review_id")
    private Integer reviewId;
    @SerializedName("course_id")
    private Integer courseId;
    @SerializedName("user_id")
    private Integer userId;
    @SerializedName("rating")
    private Integer rating;
    @SerializedName("comment")
    private String comment;
    @SerializedName("created_at")
    private LocalDateTime createdAt;
    @SerializedName("user")
    private User user;
    private Course course;

    // Constructor
    public Review() {
    }

    // Getters and Setters
    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}
