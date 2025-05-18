package com.example.mobileproject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobileproject.adapter.ReviewAdapter;
import com.example.mobileproject.adapter.TabPagerAdapter;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.Course;
import com.example.mobileproject.model.Lesson;
import com.example.mobileproject.model.Review;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {
    private static final String TAG = "CourseDetailActivity";
    private int courseId;
    private Course course;
    private List<Lesson> lessons;
    private List<Review> reviews;
    private boolean isEnrolled;
    private ReviewAdapter reviewAdapter;
    private int apiCallsCompleted = 0;
    private static final int TOTAL_API_CALLS = 4; // checkEnrollment, getCourseById, getLessons, getReviews

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        courseId = getIntent().getIntExtra("courseId", -1);
        if (courseId == -1) {
            Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RecyclerView reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        if (reviewsRecyclerView != null) {
            reviewsRecyclerView.setVisibility(View.GONE);
            Log.d(TAG, "reviewsRecyclerView initialized and set to GONE");
        } else {
            Log.e(TAG, "reviewsRecyclerView not found in layout");
        }

        ImageView backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        lessons = new ArrayList<>();
        reviews = new ArrayList<>();
        checkEnrollmentStatus();
        fetchCourseData();
        fetchLessons();
        fetchReviews();
    }

    private void checkEnrollmentStatus() {
        Integer userId = MockAuthManager.getInstance().getCurrentUserId();
        if (userId == null) {
            isEnrolled = false;
            apiCallsCompleted++;
            trySetupUI();
            Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient();
        Call<Boolean> call = apiService.checkEnrollment(courseId, userId);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isEnrolled = response.body();
                } else {
                    isEnrolled = false;
                    showErrorDialog("Failed to check enrollment status. Please try again.");
                }
                apiCallsCompleted++;
                trySetupUI();
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Error checking enrollment", t);
                isEnrolled = false;
                apiCallsCompleted++;
                trySetupUI();
                showErrorDialog("Error: " + t.getMessage() + ". Please try again.");
            }
        });
    }

    private void fetchCourseData() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            Log.d(TAG, "ProgressBar set to VISIBLE");
        }

        ApiService apiService = RetrofitClient.getClient();
        Call<Course> call = apiService.getCourseById(courseId);
        call.enqueue(new Callback<Course>() {
            @Override
            public void onResponse(Call<Course> call, Response<Course> response) {
                if (response.isSuccessful() && response.body() != null) {
                    course = response.body();
                    Log.d(TAG, "Course fetched: " + course.getTitle());
                } else {
                    Log.w(TAG, "Course fetch failed: " + response.code());
                    showErrorDialog("Failed to load course data. Please try again.");
                }
                apiCallsCompleted++;
                trySetupUI();
            }

            @Override
            public void onFailure(Call<Course> call, Throwable t) {
                Log.e(TAG, "Error fetching course data", t);
                apiCallsCompleted++;
                trySetupUI();
                showErrorDialog("Error: " + t.getMessage() + ". Please try again.");
            }
        });
    }

    private void fetchLessons() {
        ApiService apiService = RetrofitClient.getClient();
        Call<List<Lesson>> call = apiService.getLessonsByCourseId(courseId);
        call.enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lessons = response.body();
                    Log.d(TAG, "Lessons fetched: " + lessons.size());
                } else {
                    Log.w(TAG, "Lessons fetch failed: " + response.code());
                    lessons = new ArrayList<>();
                }
                apiCallsCompleted++;
                trySetupUI();
            }

            @Override
            public void onFailure(Call<List<Lesson>> call, Throwable t) {
                Log.e(TAG, "Error fetching lessons", t);
                lessons = new ArrayList<>();
                apiCallsCompleted++;
                trySetupUI();
            }
        });
    }

    private void fetchReviews() {
        ApiService apiService = RetrofitClient.getClient();
        Call<List<Review>> call = apiService.getReviewsByCourseId(courseId);
        call.enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviews = response.body();
                    Log.d(TAG, "Reviews fetched: " + reviews.size());
                } else {
                    Log.w(TAG, "Reviews fetch failed: " + response.code());
                    reviews = new ArrayList<>();
                }
                apiCallsCompleted++;
                trySetupUI();
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                Log.e(TAG, "Error fetching reviews", t);
                reviews = new ArrayList<>();
                apiCallsCompleted++;
                trySetupUI();
            }
        });
    }

    private void trySetupUI() {
        if (apiCallsCompleted >= TOTAL_API_CALLS) {
            ProgressBar progressBar = findViewById(R.id.progressBar);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "ProgressBar set to GONE");
            }
            if (course == null) {
                Log.e(TAG, "Course is null after API calls, cannot setup UI");
                showErrorDialog("Course data not available. Please try again.");
                return;
            }
            setupUI();
            setupReviewsRecyclerView();
        }
    }

    private void setupReviewsRecyclerView() {
        RecyclerView reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        if (reviewsRecyclerView == null) {
            Log.e(TAG, "reviewsRecyclerView is null in setupReviewsRecyclerView");
            return;
        }

        reviewAdapter = new ReviewAdapter(reviews);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);
        reviewsRecyclerView.setVisibility(View.VISIBLE);
        reviewAdapter.notifyDataSetChanged();
        Log.d(TAG, "reviewsRecyclerView adapter set with " + reviews.size() + " reviews and set to VISIBLE");
    }

    private void setupUI() {
        ImageView headerImage = findViewById(R.id.header_image);
        ImageView instructorImage = findViewById(R.id.instructor_image);
        TextView instructorName = findViewById(R.id.instructor_name);
        TextView courseTitle = findViewById(R.id.course_title);
        TextView courseDuration = findViewById(R.id.course_duration);
        TextView lessonCount = findViewById(R.id.lesson_count);
        TextView courseRating = findViewById(R.id.course_rating);
        TextView studentCount = findViewById(R.id.student_count);
        TextView courseDescription = findViewById(R.id.course_description);

        if (headerImage != null) {
            headerImage.setImageResource(R.drawable.course_image);
        }
        if (instructorImage != null) {
            instructorImage.setImageResource(R.drawable.teacher);
        }
        if (instructorName != null) {
            instructorName.setText(course.getInstructor() != null && course.getInstructor().getFullName() != null
                    ? course.getInstructor().getFullName() : "Unknown");
        }
        if (courseTitle != null) {
            courseTitle.setText(course.getTitle() != null ? course.getTitle() : "No Title");
        }
        if (courseDuration != null) {
            courseDuration.setText(formatDuration(lessons));
        }
        if (lessonCount != null) {
            lessonCount.setText(lessons.size() + " Lessons");
        }
        if (courseRating != null) {
            courseRating.setText(String.format("%.1f (%d)", calculateAverageRating(reviews), reviews.size()));
        }
        if (studentCount != null) {
            studentCount.setText((course.getUsers() != null ? course.getUsers().size() : 0) + " students");
        }
        if (courseDescription != null) {
            courseDescription.setText(course.getDescription() != null ? course.getDescription() : "No Description");
        }

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        if (tabLayout != null && viewPager != null) {
            List<String> tabTitles = new ArrayList<>();
            tabTitles.add("Overview");
            tabTitles.add("Lessons");

            TabPagerAdapter adapter = new TabPagerAdapter(this, course, lessons, reviews);
            viewPager.setAdapter(adapter);
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabTitles.get(position))).attach();
            Log.d(TAG, "TabPagerAdapter set for ViewPager with course: " + course.getTitle());
        } else {
            Log.e(TAG, "TabLayout or ViewPager is null");
        }

        RatingBar ratingBar = findViewById(R.id.ratingBar);
        EditText reviewCommentInput = findViewById(R.id.reviewCommentInput);
        Button submitReviewButton = findViewById(R.id.submitReviewButton);

        if (submitReviewButton != null) {
            submitReviewButton.setOnClickListener(v -> {
                if (!isEnrolled) {
                    Toast.makeText(this, "You must enroll in the course to leave a review", Toast.LENGTH_SHORT).show();
                    return;
                }
                float rating = ratingBar != null ? ratingBar.getRating() : 0;
                String comment = reviewCommentInput != null ? reviewCommentInput.getText().toString().trim() : "";
                if (rating > 0 && !comment.isEmpty()) {
                    Integer userId = MockAuthManager.getInstance().getCurrentUserId();
                    if (userId == null) {
                        Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Review newReview = new Review();
                    newReview.setRating((int) rating);
                    newReview.setComment(comment);
                    newReview.setCourseId(courseId);
                    newReview.setUserId(userId);
                    newReview.setCreatedAt(LocalDateTime.now());
                    newReview.setUser(MockAuthManager.getInstance().getCurrentUser());
                    addReviewToServer(newReview);
                } else {
                    Toast.makeText(this, "Please provide a rating and comment", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addReviewToServer(Review review) {
//        ApiService apiService = RetrofitClient.getClient();
//        Call<Review> call = apiService.addReview(review);
//        call.enqueue(new Callback<Review>() {
//            @Override
//            public void onResponse(Call<Review> call, Response<Review> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    reviews.add(response.body());
//                    if (reviewAdapter != null) {
//                        reviewAdapter.setData(reviews);
//                        reviewAdapter.notifyItemInserted(reviews.size() - 1);
//                        Log.d(TAG, "New review added and adapter notified");
//                    } else {
//                        Log.w(TAG, "reviewAdapter is null when adding review");
//                    }
//                    EditText reviewCommentInput = findViewById(R.id.reviewCommentInput);
//                    RatingBar ratingBar = findViewById(R.id.ratingBar);
//                    if (reviewCommentInput != null) {
//                        reviewCommentInput.setText("");
//                    }
//                    if (ratingBar != null) {
//                        ratingBar.setRating(0);
//                    }
//                    Toast.makeText(CourseDetailActivity.this, "Review submitted", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(CourseDetailActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Review> call, Throwable t) {
//                Log.e(TAG, "Error adding review", t);
//                Toast.makeText(CourseDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> {
                    apiCallsCompleted = 0;
                    checkEnrollmentStatus();
                    fetchCourseData();
                    fetchLessons();
                    fetchReviews();
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
    }

    private String formatDuration(List<Lesson> lessons) {
        int totalSeconds = 0;
        for (Lesson lesson : lessons) {
            totalSeconds += lesson.getDuration() != null ? lesson.getDuration() : 0;
        }
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d mins", minutes, seconds);
    }

    private float calculateAverageRating(List<Review> reviews) {
        if (reviews.isEmpty()) return 0.0f;
        float sum = 0;
        for (Review review : reviews) {
            sum += review.getRating() != null ? review.getRating() : 0;
        }
        return sum / reviews.size();
    }
}