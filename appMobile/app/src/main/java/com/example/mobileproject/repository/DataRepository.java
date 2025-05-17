package com.example.mobileproject.repository;

import com.example.mobileproject.model.Category;
import com.example.mobileproject.model.Course;
import com.example.mobileproject.model.User;

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
    public static List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();

        // Design courses
        courses.add(new Course(
                "d1",
                "Coding with Python Interface",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://images.unsplash.com/photo-1587620962725-abab7fe55159",
                "Design"
        ));

        courses.add(new Course(
                "d2",
                "Design with the Environment",
                "Stephen Morris",
                14.50,
                4.3f,
                false,
                "https://images.unsplash.com/photo-1606857521015-7f9fcf423740",
                "Design"
        ));

        courses.add(new Course(
                "d3",
                "Coding with Python Interface",
                "Stephen Morris",
                14.50,
                4.8f,
                true,
                "https://images.unsplash.com/photo-1618761714954-0b8cd0026356",
                "Design"
        ));

        courses.add(new Course(
                "d4",
                "Music with the Environment",
                "Stephen Morris",
                14.50,
                4.2f,
                false,
                "https://images.unsplash.com/photo-1542626991-cbc4e32524cc",
                "Design"
        ));

        courses.add(new Course(
                "d5",
                "Skips Skills with the Environment",
                "Stephen Morris",
                14.50,
                5.0f,
                true,
                "https://images.unsplash.com/photo-1542751371-adc38448a05e",
                "Design"
        ));

        courses.add(new Course(
                "d6",
                "UI/UX Basics for Beginners",
                "Stephen Morris",
                14.50,
                4.7f,
                true,
                "https://images.unsplash.com/photo-1593642532744-d377ab507dc8",
                "Design"
        ));

        // Development courses
        courses.add(new Course(
                "dev1",
                "React for Beginners",
                "John Smith",
                19.99,
                4.7f,
                true,
                "https://images.unsplash.com/photo-1633356122544-f134324a6cee",
                "Development"
        ));

        courses.add(new Course(
                "dev2",
                "Node.js Fundamentals",
                "Maria Garcia",
                24.99,
                4.5f,
                false,
                "https://images.unsplash.com/photo-1504639725590-34d0984388bd",
                "Development"
        ));

        courses.add(new Course(
                "dev3",
                "Mobile App Development",
                "Ahmed Khan",
                29.99,
                4.8f,
                true,
                "https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb",
                "Development"
        ));

        courses.add(new Course(
                "dev4",
                "Python for Data Science",
                "Lisa Johnson",
                22.99,
                4.6f,
                false,
                "https://images.unsplash.com/photo-1516116216624-53e697fedbea",
                "Development"
        ));

        courses.add(new Course(
                "dev5",
                "Java Programming Masterclass",
                "Robert Chen",
                18.99,
                4.4f,
                true,
                "https://images.unsplash.com/photo-1517694712202-14dd9538aa97",
                "Development"
        ));

        // IT & Software courses
        courses.add(new Course(
                "it1",
                "CompTIA A+ Certification",
                "Michael Brown",
                49.99,
                4.8f,
                true,
                "https://images.unsplash.com/photo-1531297484001-80022131f5a1",
                "IT & Software"
        ));

        courses.add(new Course(
                "it2",
                "AWS Solutions Architect",
                "Jennifer Lee",
                59.99,
                4.7f,
                true,
                "https://images.unsplash.com/photo-1489389944381-3471b5b30f04",
                "IT & Software"
        ));

        courses.add(new Course(
                "it3",
                "Cybersecurity Fundamentals",
                "David Wilson",
                39.99,
                4.5f,
                false,
                "https://images.unsplash.com/photo-1563206767-5b18f218e8de",
                "IT & Software"
        ));

        // Health & Fitness courses
        courses.add(new Course(
                "hf1",
                "Yoga for Beginners",
                "Sarah Johnson",
                12.99,
                4.6f,
                false,
                "https://images.unsplash.com/photo-1545205597-3d9d02c29597",
                "Health & Fitness"
        ));

        courses.add(new Course(
                "hf2",
                "Nutrition Fundamentals",
                "James Wilson",
                14.99,
                4.4f,
                false,
                "https://images.unsplash.com/photo-1490645935967-10de6ba17061",
                "Health & Fitness"
        ));

        courses.add(new Course(
                "hf3",
                "Home Workout Routines",
                "Emily Davis",
                9.99,
                4.3f,
                true,
                "https://images.unsplash.com/photo-1517836357463-d25dfeac3438",
                "Health & Fitness"
        ));

        return courses;
    }

    // Lấy khóa học theo category
    public static List<Course> getCoursesByCategory(String category) {
        List<Course> allCourses = getAllCourses();
        List<Course> filteredCourses = new ArrayList<>();

        for (Course course : allCourses) {
            if (course.getCategory() != null && course.getCategory().equals(category)) {
                filteredCourses.add(course);
            }
        }

        return filteredCourses;
    }

    // Các phương thức khác giữ nguyên
    // Phương thức trả về thông tin người dùng hiện tại (giả lập)
    public static User getCurrentUser() {
        return new User(
                "user1",
                "Alex Joe",
                "alexjoe@example.com",
                "https://randomuser.me/api/portraits/men/32.jpg"
        );
    }

    // Mock data for courses with image URLs (giữ lại để tương thích với code cũ)
    public static List<Course> getMockCourses() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course(
                "c1",
                "Generator on there Internet trend",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://randomuser.me/api/portraits/men/32.jpg",
                "Design"
        ));

        courses.add(new Course(
                "c2",
                "Generator on there Internet trend",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://randomuser.me/api/portraits/women/44.jpg",
                "Development"
        ));

        courses.add(new Course(
                "c3",
                "Generator on there Internet trend",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://randomuser.me/api/portraits/men/32.jpg",
                "Design"
        ));

        courses.add(new Course(
                "c4",
                "Generator on there Internet trend",
                "Stephen Morris",
                14.50,
                4.5f,
                true,
                "https://randomuser.me/api/portraits/women/44.jpg",
                "IT & Software"
        ));

        return courses;
    }

    // URL ảnh cho banner
    // URL ảnh cho banner
    public static String[] getBannerImageUrls() {
        return new String[] {
                "https://images.unsplash.com/photo-1580894742597-87bc8789db3d?q=80&w=2670&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1594904351111-a072f80b1a71?q=80&w=2670&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1501504905252-473c47e087f8?q=80&w=2674&auto=format&fit=crop"
        };
    }
}