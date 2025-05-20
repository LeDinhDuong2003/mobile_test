package com.example.mobileproject.model;

import com.google.gson.annotations.SerializedName;

public class WishlistResponse {
    @SerializedName("wishlist_id")
    private int wishlistId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("course_id")
    private int courseId;

    @SerializedName("created_at")
    private String createdAt;

    public int getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(int wishlistId) {
        this.wishlistId = wishlistId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}