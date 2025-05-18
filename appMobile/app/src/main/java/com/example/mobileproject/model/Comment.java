package com.example.mobileproject.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Comment implements Serializable {
    @SerializedName("comment_id")
    private Integer commentId;
    @SerializedName("user_id")
    private Integer userId;
    @SerializedName("lesson_id")
    private Integer lessonId;
    @SerializedName("comment")
    private String comment;
    @SerializedName("created_at")
    private LocalDateTime createdAt;
    @SerializedName("user")
    private User user;
    private Lesson lesson;
    // Constructor
    public Comment() {
    }

    // Getters and Setters
    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getLessonId() { return lessonId; }
    public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
}