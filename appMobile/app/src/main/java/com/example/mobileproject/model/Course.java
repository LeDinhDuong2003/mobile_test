package com.example.mobileproject.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Course implements Serializable {
    @SerializedName("course_id")
    private Integer courseId;
    @SerializedName("owner_id")
    private Integer ownerId;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    @SerializedName("created_at")
    private LocalDateTime createdAt;
    @SerializedName("price")
    private Float price;
    @SerializedName("instructor")
    private User instructor;
    private List<Wishlist> wishlists;
    private List<Enrollment> enrollments;
    private List<Lesson> lessons;
    private List<Review> reviews;

    // Constructor
    public Course() {
        this.wishlists = new ArrayList<>();
        this.enrollments = new ArrayList<>();
        this.lessons = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    // Getters and Setters
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }
    public User getInstructor() { return instructor; }
    public void setInstructor(User instructor) { this.instructor = instructor; }
    public List<Wishlist> getWishlists() { return wishlists; }
    public void setWishlists(List<Wishlist> wishlists) { this.wishlists = wishlists; }
    public List<Enrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(List<Enrollment> enrollments) { this.enrollments = enrollments; }
    public List<Lesson> getLessons() { return lessons; }
    public void setLessons(List<Lesson> lessons) { this.lessons = lessons; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }

    public List<User> getUsers() {
        return enrollments.stream()
                .map(Enrollment::getUser)
                .collect(Collectors.toList());
    }

}