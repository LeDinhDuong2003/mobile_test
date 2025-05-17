package com.example.mobileproject.repository;

import com.example.mobileproject.model.Course;

import java.util.ArrayList;
import java.util.List;

public class DataRepository {

    // Mock data for courses with image URLs
    public static List<Course> getMockCourses() {
        List<Course> courses = new ArrayList<>();

        // Sử dụng URL hình ảnh thay vì resource ID
        courses.add(new Course(
                "c1",
                "Generator on there Internet trend",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://randomuser.me/api/portraits/men/32.jpg"
        ));

        courses.add(new Course(
                "c2",
                "Generator on there Internet trend",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://randomuser.me/api/portraits/women/44.jpg"
        ));

        courses.add(new Course(
                "c3",
                "Generator on there Internet trend",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://randomuser.me/api/portraits/men/32.jpg"
        ));

        courses.add(new Course(
                "c4",
                "Generator on there Internet trend",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://randomuser.me/api/portraits/women/44.jpg"
        ));

        return courses;
    }

    // URL hình ảnh banner cố định
    public static String getBannerImageUrl() {
        return "https://randomuser.me/api/portraits/women/68.jpg";
    }

    // Get courses by category
    public static List<Course> getCoursesByCategory(String category) {
        // In a real app, this would filter courses by category
        // For now, return all mock courses
        return getMockCourses();
    }

    // Get course by ID
    public static Course getCourseById(String courseId) {
        List<Course> courses = getMockCourses();
        for (Course course : courses) {
            if (course.getId().equals(courseId)) {
                return course;
            }
        }
        return null;
    }
}
