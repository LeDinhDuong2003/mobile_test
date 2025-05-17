package com.example.mobileproject.model;


public class Course {
    private String id;
    private String title;
    private String authorName;
    private double price;
    private float rating;
    private boolean isBestSeller;
    private String imageUrl;

    public Course(String id, String title, String authorName, double price, float rating,
                  boolean isBestSeller, String imageUrl) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
        this.price = price;
        this.rating = rating;
        this.isBestSeller = isBestSeller;
        this.imageUrl = imageUrl;
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
}
