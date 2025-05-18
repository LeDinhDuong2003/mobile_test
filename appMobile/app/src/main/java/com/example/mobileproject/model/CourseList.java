package com.example.mobileproject.model;

public class CourseList {
    private String id;
    private String title;
    private String authorName;
    private double price;
    private float rating;
    private boolean isBestSeller;
    private String imageUrl;
    private String category;

    public CourseList(String id, String title, String authorName, double price, float rating,
                      boolean isBestSeller, String imageUrl, String category) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
        this.price = price;
        this.rating = rating;
        this.isBestSeller = isBestSeller;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public double getPrice() {
        return price;
    }

    public float getRating() {
        return rating;
    }

    public boolean isBestSeller() {
        return isBestSeller;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }
}