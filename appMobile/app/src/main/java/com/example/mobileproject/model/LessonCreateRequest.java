package com.example.mobileproject.model;

import com.google.gson.annotations.SerializedName;

public class LessonCreateRequest {
    @SerializedName("title")
    private String title;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("duration")
    private int duration;

    @SerializedName("position")
    private int position;

    @SerializedName("course_id")
    private int courseId;

    public LessonCreateRequest() {}

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
}