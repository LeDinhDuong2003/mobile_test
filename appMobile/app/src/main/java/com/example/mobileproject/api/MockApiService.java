//// app/src/main/java/com/example/mobileproject/api/MockApiService.java
//package com.example.mobileproject.api;
//
//import com.example.mobileproject.model.Comment;
//import com.example.mobileproject.model.Course;
//import com.example.mobileproject.model.Enrollment;
//import com.example.mobileproject.model.Lesson;
//import com.example.mobileproject.model.Review;
//import com.example.mobileproject.model.User;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.mock.BehaviorDelegate;
//
//public class MockApiService implements ApiService {
//    private final BehaviorDelegate<ApiService> delegate;
//    private final List<Course> mockCourses;
//    private final List<Lesson> mockLessons;
//    private final List<Review> mockReviews;
//    private final List<Comment> mockComments;
//    private final List<Integer> enrolledUsers;
//
//    public MockApiService(BehaviorDelegate<ApiService> delegate) {
//        this.delegate = delegate;
//
//        User instructor = new User();
//        instructor.setUserId(1);
//        instructor.setFullName("John Doe");
//        instructor.setRole("Instructor");
//
//        User student = new User();
//        student.setUserId(2);
//        student.setFullName("Jane Smith");
//        student.setRole("Student");
//
//        mockLessons = new ArrayList<>();
//        Lesson lesson1 = new Lesson();
//        lesson1.setLessonId(1);
//        lesson1.setCourseId(1);
//        lesson1.setTitle("Introduction");
//        lesson1.setVideoUrl("http://example.com/video1.mp4");
//        lesson1.setDuration(300);
//        lesson1.setPosition(1);
//        mockLessons.add(lesson1);
//
//        Lesson lesson2 = new Lesson();
//        lesson2.setLessonId(2);
//        lesson2.setCourseId(1);
//        lesson2.setTitle("Advanced Topics");
//        lesson2.setVideoUrl("http://example.com/video2.mp4");
//        lesson2.setDuration(600);
//        lesson2.setPosition(2);
//        mockLessons.add(lesson2);
//
//        mockReviews = new ArrayList<>();
//        Review review1 = new Review();
//        review1.setReviewId(1);
//        review1.setCourseId(1);
//        review1.setUserId(2);
//        review1.setRating(4);
//        review1.setComment("Great course!");
//        review1.setUser(student);
//        review1.setCreatedAt(LocalDateTime.now());
//        mockReviews.add(review1);
//
//        Review review2 = new Review();
//        review2.setReviewId(2);
//        review2.setCourseId(1);
//        review2.setUserId(2);
//        review2.setRating(5);
//        review2.setComment("Very informative!");
//        review2.setUser(student);
//        review2.setCreatedAt(LocalDateTime.now());
//        mockReviews.add(review2);
//
//        mockComments = new ArrayList<>();
//        Comment comment1 = new Comment();
//        comment1.setCommentId(1);
//        comment1.setLessonId(1);
//        comment1.setUserId(2);
//        comment1.setComment("This lesson is clear!");
//        comment1.setUser(student);
//        comment1.setCreatedAt(LocalDateTime.now());
//        mockComments.add(comment1);
//
//        Comment comment2 = new Comment();
//        comment2.setCommentId(2);
//        comment2.setLessonId(1);
//        comment2.setUserId(2);
//        comment2.setComment("Loved the examples!");
//        comment2.setUser(student);
//        comment2.setCreatedAt(LocalDateTime.now());
//        mockComments.add(comment2);
//
//        mockCourses = new ArrayList<>();
//        Course course = new Course();
//        course.setCourseId(1);
//        course.setTitle("Android Development");
//        course.setDescription("Learn Android from scratch");
//        course.setInstructor(instructor);
//        course.setLessons(mockLessons);
//        course.setReviews(mockReviews);
//
//        // Fix: Create Enrollment instance explicitly
//        Enrollment enrollment = new Enrollment();
//        enrollment.setUser(student);
//        enrollment.setCourseId(1);
//        course.setEnrollments(Arrays.asList(enrollment));
//
//        mockCourses.add(course);
//
//        enrolledUsers = new ArrayList<>();
//        enrolledUsers.add(1); // userId 1 is enrolled
//    }
//
//    @Override
//    public Call<Course> getCourseById(int id) {
//        Course course = mockCourses.stream().filter(c -> c.getCourseId() == id).findFirst().orElse(null);
//        return delegate.returningResponse(course).getCourseById(id);
//    }
//
//    @Override
//    public Call<Lesson> getLessonById(int id) {
//        Lesson lesson = mockLessons.stream().filter(l -> l.getLessonId() == id).findFirst().orElse(null);
//        return delegate.returningResponse(lesson).getLessonById(id);
//    }
//
//    @Override
//    public Call<List<Lesson>> getLessonsByCourseId(int courseId) {
//        List<Lesson> lessons = new ArrayList<>();
//        for (Lesson lesson : mockLessons) {
//            if (lesson.getCourseId() == courseId) {
//                lessons.add(lesson);
//            }
//        }
//        return delegate.returningResponse(lessons).getLessonsByCourseId(courseId);
//    }
//
//    @Override
//    public Call<List<Comment>> getCommentsByLessonId(int lessonId) {
//        List<Comment> comments = new ArrayList<>();
//        for (Comment comment : mockComments) {
//            if (comment.getLessonId() == lessonId) {
//                comments.add(comment);
//            }
//        }
//        return delegate.returningResponse(comments).getCommentsByLessonId(lessonId);
//    }
//
//    @Override
//    public Call<Review> addReview(Review review) {
//        review.setReviewId(mockReviews.size() + 1);
//        mockReviews.add(review);
//        return delegate.returningResponse(review).addReview(review);
//    }
//
//    @Override
//    public Call<Comment> addComment(Comment comment) {
//        comment.setCommentId(mockComments.size() + 1);
//        mockComments.add(comment);
//        return delegate.returningResponse(comment).addComment(comment);
//    }
//
//    @Override
//    public Call<Boolean> checkEnrollment(int courseId, int userId) {
//        boolean isEnrolled = enrolledUsers.contains(userId) && mockCourses.stream()
//                .anyMatch(c -> c.getCourseId() == courseId);
//        return delegate.returningResponse(isEnrolled).checkEnrollment(courseId, userId);
//    }
//}