package com.example.mobileproject.repository;

import com.example.mobileproject.R;
import com.example.mobileproject.model.Category;
import com.example.mobileproject.model.CourseList;
import com.example.mobileproject.model.UserMain;

import java.util.ArrayList;
import java.util.List;

public class DataRepository {

    // Lấy danh sách categories
    public static List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("c1", "Design"));
        categories.add(new Category("c2", "Development"));
        categories.add(new Category("c3", "IT & Software"));
        categories.add(new Category("c4", "Health & Fitness"));
        categories.add(new Category("c5", "Marketing"));
        categories.add(new Category("c6", "Business"));
        return categories;
    }

    // Lấy tất cả khóa học (mở rộng với nhiều khóa học hơn và thêm category)
    public static List<CourseList> getAllCourses() {
        List<CourseList> courses = new ArrayList<>();

        // Design courses
        courses.add(new CourseList(
                "d1",
                "Coding with Python Interface",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://images.unsplash.com/photo-1587620962725-abab7fe55159",
                "Design"
        ));

        courses.add(new CourseList(
                "d2",
                "Design with the Environment",
                "Stephen Morris",
                14.50,
                4.3f,
                false,
                "https://images.unsplash.com/photo-1606857521015-7f9fcf423740",
                "Design"
        ));

        // Thêm các khóa học khác...

        return courses;
    }

    // Lấy khóa học theo category
    public static List<CourseList> getCoursesByCategory(String category) {
        List<CourseList> allCourses = getAllCourses();
        List<CourseList> filteredCourses = new ArrayList<>();

        for (CourseList course : allCourses) {
            if (course.getCategory() != null && course.getCategory().equals(category)) {
                filteredCourses.add(course);
            }
        }

        return filteredCourses;
    }

    // Phương thức trả về thông tin người dùng hiện tại (giả lập)
    public static UserMain getCurrentUser() {
        return new UserMain(
                "user1",
                "Alex Joe",
                "alexjoe@example.com",
                "https://randomuser.me/api/portraits/men/32.jpg"
        );
    }

    // Mock data for courses with image URLs (giữ lại để tương thích với code cũ)
    public static List<CourseList> getMockCourses() {
        List<CourseList> courses = new ArrayList<>();
        courses.add(new CourseList(
                "c1",
                "Generator on there Internet trend",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://randomuser.me/api/portraits/men/32.jpg",
                "Design"
        ));

        // Thêm các khóa học khác...

        return courses;
    }

    // Thay đổi để trả về resource IDs thay vì URLs
    public static int[] getBannerImageResources() {
        return new int[] {
                R.drawable.b1,
                R.drawable.b2,
                R.drawable.b3
        };
    }
}