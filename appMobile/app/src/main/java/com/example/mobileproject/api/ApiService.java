// app/src/main/java/com/example/mobileproject/api/ApiService.java
package com.example.mobileproject.api;

import com.example.mobileproject.model.Comment;
import com.example.mobileproject.model.Course;
import com.example.mobileproject.model.CourseResponse;
import com.example.mobileproject.model.FCMTokenRequest;
import com.example.mobileproject.model.FCMTokenResponse;
import com.example.mobileproject.model.Lesson;
import com.example.mobileproject.model.NotificationModel;
import com.example.mobileproject.model.PagedResponse;
import com.example.mobileproject.model.Review;
import com.example.mobileproject.model.WishlistRequest;
import com.example.mobileproject.model.WishlistResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("courses/{id}")
    Call<Course> getCourseById(@Path("id") int id);

    @GET("lessons/{id}")
    Call<Lesson> getLessonById(@Path("id") int id);

    @GET("courses/{courseId}/lessons")
    Call<List<Lesson>> getLessonsByCourseId(@Path("courseId") int courseId);

    @GET("courses/{courseId}/reviews")
    Call<List<Review>> getReviewsByCourseId(@Path("courseId") int courseId);

    @GET("lessons/{lessonId}/comments")
    Call<List<Comment>> getCommentsByLessonId(@Path("lessonId") int lessonId);

    @POST("courses/{courseId}/reviews")
    Call<Review> addReview(@Path("courseId") int courseId, @Body Review review);

    @POST("lessons/{lessonId}/comments")
    Call<Comment> addComment(@Path("lessonId") int lessonId, @Body Comment comment);

    @GET("courses/{courseId}/users/{userId}/enrollment")
    Call<Boolean> checkEnrollment(@Path("courseId") int courseId, @Path("userId") int userId);

    @GET("api/courses/top")
    Call<List<CourseResponse>> getTopCourses();

    // Lấy chi tiết một khóa học
    @GET("api/courses/{course_id}")
    Call<CourseResponse> getCourseById(@Path("course_id") String courseId);

    // API chung: lấy danh sách khóa học, có thể tìm kiếm, lọc theo danh mục và phân trang
    @GET("api/courses")
    Call<PagedResponse<CourseResponse>> getCourses(
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("category") String category,
            @Query("query") String query);

    @GET("users/{userId}/notifications")
    Call<List<NotificationModel>> getUserNotifications(@Path("userId") int userId);

    @POST("users/{userId}/notifications/{notificationId}/read")
    Call<Void> markNotificationAsRead(@Path("userId") int userId, @Path("notificationId") int notificationId);

    @POST("users/{userId}/fcm-token")
    Call<FCMTokenResponse> updateFCMToken(
            @Path("userId") int userId,
            @Body FCMTokenRequest request
    );

    @GET("users/{userId}/wishlists")
    Call<List<CourseResponse>> getUserWishlists(@Path("userId") int userId);

    @POST("wishlists/add")
    Call<WishlistResponse> addToWishlist(@Body WishlistRequest request);

    @POST("wishlists/remove")
    Call<Void> removeFromWishlist(@Body WishlistRequest request);

    @GET("wishlists/check")
    Call<Boolean> checkWishlist(@Query("userId") int userId, @Query("courseId") int courseId);
}