package com.example.mobileproject.model;

import com.google.gson.annotations.SerializedName;

public class CourseResponse {
    @SerializedName("course_id")
    private int courseId;

    private String title;

    private String description;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    private double price;

    private float rating;

    @SerializedName("instructor_name")
    private String instructorName;

    @SerializedName("is_bestseller")
    private boolean isBestseller;

    private String category;

    // Getters và Setters
    public int getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public double getPrice() {
        return price;
    }

    public float getRating() {
        return rating;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public boolean isBestseller() {
        return isBestseller;
    }

    public String getCategory() {
        return category;
    }

    // Phương thức chuyển đổi từ CourseResponse sang Course
    public CourseList toCourse() {
        return new CourseList(
                String.valueOf(courseId),
                title,
                instructorName,
                price,
                rating,
                isBestseller,
                thumbnailUrl,
                category
        );
    }
}