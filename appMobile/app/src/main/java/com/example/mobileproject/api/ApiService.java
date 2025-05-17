package com.example.mobileproject.api;

import com.example.mobileproject.model.Course;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface for API calls - ready for future implementation with real API
 * Currently not in use, just showing the structure for future expansion
 */
public interface ApiService {

    @GET("courses")
    Call<List<Course>> getCourses();

    @GET("courses/top")
    Call<List<Course>> getTopCourses();

    @GET("courses/{id}")
    Call<Course> getCourseById(@Path("id") String courseId);

    @GET("courses/category/{category}")
    Call<List<Course>> getCoursesByCategory(@Path("category") String category);

    @GET("courses/search")
    Call<List<Course>> searchCourses(@Query("query") String query);
}
