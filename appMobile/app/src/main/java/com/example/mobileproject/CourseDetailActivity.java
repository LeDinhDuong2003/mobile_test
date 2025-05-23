package com.example.mobileproject;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.mobileproject.adapter.ReviewAdapter;
import com.example.mobileproject.adapter.TabPagerAdapter;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.Course;
import com.example.mobileproject.model.Enrollment;
import com.example.mobileproject.model.Lesson;
import com.example.mobileproject.model.PaginatedReviewsResponse;
import com.example.mobileproject.model.Review;
import com.example.mobileproject.model.User;
import com.example.mobileproject.model.WishlistRequest;
import com.example.mobileproject.model.WishlistResponse;
import com.example.mobileproject.util.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseDetailActivity extends AppCompatActivity {
    private static final String TAG = "CourseDetailActivity";
    private int courseId;
    private Course course;
    private List<Lesson> lessons;
    private List<Review> reviews;
    private boolean isEnrolled;
    private ReviewAdapter reviewAdapter;
    private int apiCallsCompleted = 0;
    private int enrollmentCount = 0;

    private static final int PAGE_SIZE = 5; // Match the comments setup
    private int currentPage = 1; // Track the current page
    private int totalPages = 1; // Track total pages from the response
//    private static final int TOTAL_API_CALLS = 4; // checkEnrollment, getCourseById, getLessons, getReviews

    Button enrollButton;
    private static final int TOTAL_API_CALLS = 5; // checkEnrollment, getCourseById, getLessons, getReviews, checkWishlist

    // New variables for wishlist functionality
    private ImageButton favoriteButton;
    private boolean isInWishlist = false;
    private SessionManager sessionManager;

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

        enrollButton = findViewById(R.id.enrollButton);

        // Xử lý sự kiện khi nhấn nút (tuỳ chọn)
        enrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);

                if(userId == -1){
                    Toast.makeText(CourseDetailActivity.this, "User data not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                ApiService apiService = RetrofitClient.getClient();
                Map<String, Integer> requestBody = new HashMap<>();
                requestBody.put("user_id", userId);
                Call<Enrollment> call = apiService.enrollInCourse(courseId, requestBody);
                call.enqueue(new Callback<Enrollment>() {
                    @Override
                    public void onResponse(Call<Enrollment> call, Response<Enrollment> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            isEnrolled = true;
                            updateEnrollButtonVisibility();
                            Toast.makeText(CourseDetailActivity.this, "Successfully enrolled in the course!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CourseDetailActivity.this, "Failed to enroll. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Enrollment> call, Throwable t) {
                        Log.e(TAG, "Error enrolling in course", t);
                        Toast.makeText(CourseDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        sessionManager = SessionManager.getInstance(this);

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

        // Initialize favorite button
        favoriteButton = findViewById(R.id.favoriteButton);
        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v -> toggleWishlist());
        }

        lessons = new ArrayList<>();
        reviews = new ArrayList<>();
        checkEnrollmentStatus();
        fetchCourseData();
        fetchLessons();
        fetchReviews(currentPage);
        checkWishlistStatus();
        fetchEnrollmentCount();
    }

    private void checkWishlistStatus() {
        Integer userId = sessionManager.getUserId();
        if (userId == null || userId == -1) {
            Log.e(TAG, "Invalid user ID");
            return;
        }

        ApiService apiService = RetrofitClient.getClient();
        Call<Boolean> call = apiService.checkWishlist(userId, courseId);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isInWishlist = response.body();
                    updateFavoriteButton();
                } else {
                    isInWishlist = false;
                    updateFavoriteButton();
                }

                apiCallsCompleted++;
                trySetupUI();
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Error checking wishlist status", t);
                isInWishlist = false;
                updateFavoriteButton();

                apiCallsCompleted++;
                trySetupUI();
            }
        });
    }

    private void updateFavoriteButton() {
        if (favoriteButton != null) {
            favoriteButton.setImageResource(isInWishlist ?
                    R.drawable.ic_favorite_filled :
                    R.drawable.ic_favorite_border);
        }
    }

    private void toggleWishlist() {
        Integer userId = sessionManager.getUserId();
        if (userId == null || userId == -1) {
            Toast.makeText(this, "Please login to add to favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient();
        WishlistRequest request = new WishlistRequest(userId, courseId);

        if (isInWishlist) {
            // Remove from wishlist
            Call<Void> call = apiService.removeFromWishlist(request);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        isInWishlist = false;
                        updateFavoriteButton();
                        Toast.makeText(CourseDetailActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CourseDetailActivity.this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(CourseDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add to wishlist
            Call<WishlistResponse> call = apiService.addToWishlist(request);
            call.enqueue(new Callback<WishlistResponse>() {
                @Override
                public void onResponse(Call<WishlistResponse> call, Response<WishlistResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        isInWishlist = true;
                        updateFavoriteButton();
                        Toast.makeText(CourseDetailActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CourseDetailActivity.this, "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<WishlistResponse> call, Throwable t) {
                    Toast.makeText(CourseDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateEnrollButtonVisibility() {
        enrollButton.setVisibility(isEnrolled ? View.GONE : View.VISIBLE);
    }

    private void checkEnrollmentStatus() {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
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
                    updateEnrollButtonVisibility();
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

    private void fetchEnrollmentCount() {
        ApiService apiService = RetrofitClient.getClient();
        Call<Integer> call = apiService.getEnrollmentCount(courseId);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    enrollmentCount = response.body();
                    Log.d(TAG, "Enrollment count fetched: " + enrollmentCount);
                } else {
                    Log.w(TAG, "Enrollment count fetch failed: " + response.code());
                    enrollmentCount = 0;
                }
                apiCallsCompleted++;
                trySetupUI();
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, "Error fetching enrollment count", t);
                enrollmentCount = 0;
                apiCallsCompleted++;
                trySetupUI();
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

    private void fetchReviews(int page) {
        Log.d(TAG, "Fetching reviews for page: " + page);
        currentPage = page; // Set the current page before the API call

        ApiService apiService = RetrofitClient.getClient();
        Call<PaginatedReviewsResponse> call = apiService.getReviewsByCourseId(courseId, page, PAGE_SIZE);
        call.enqueue(new Callback<PaginatedReviewsResponse>() {
            @Override
            public void onResponse(Call<PaginatedReviewsResponse> call, Response<PaginatedReviewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PaginatedReviewsResponse paginatedResponse = response.body();
                    Log.d(TAG, "API Response: currentPage=" + paginatedResponse.getPagination().getCurrentPage() +
                            ", totalPages=" + paginatedResponse.getPagination().getTotalPages() +
                            ", reviewCount=" + paginatedResponse.getData().size());
                    reviews = paginatedResponse.getData();
                    currentPage = paginatedResponse.getPagination().getCurrentPage();
                    totalPages = paginatedResponse.getPagination().getTotalPages();
                } else {
                    Log.w(TAG, "Reviews fetch failed: " + response.code());
                    reviews = new ArrayList<>();
                }
                apiCallsCompleted++;
                trySetupUI();
            }

            @Override
            public void onFailure(Call<PaginatedReviewsResponse> call, Throwable t) {
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

        if (headerImage != null && course.getThumbnailUrl() != null) {
            Glide.with(this)
                    .load(course.getThumbnailUrl())
                    .placeholder(R.drawable.placeholder_image) // ảnh tạm khi đang tải
                    .error(R.drawable.error_image)             // ảnh khi tải lỗi
                    .into(headerImage);
        }
        if (instructorImage != null) {
            Glide.with(this)
                    .load(course.getInstructor().getAvatarUrl())
                    .placeholder(R.drawable.placeholder_image) // ảnh tạm khi đang tải
                    .error(R.drawable.error_image)             // ảnh khi tải lỗi
                    .into(instructorImage);
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
            studentCount.setText(enrollmentCount + " students");
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
                    SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                    int userId = prefs.getInt("user_id", -1);
                    if (userId == -1) {
                        Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Review newReview = new Review();
                    newReview.setRating((int) rating);
                    newReview.setComment(comment);
                    newReview.setCourseId(courseId);
                    newReview.setUserId(userId);
                    newReview.setCreatedAt(LocalDateTime.now());

                    User user = new User();

                    user.setUserId(prefs.getInt("user_id", -1));
                    user.setFullName(prefs.getString("full_name", null));
                    user.setEmail(prefs.getString("email", null));
                    user.setAvatarUrl(prefs.getString("avatar_url", null));
                    user.setPhone(prefs.getString("phone", null));
                    user.setRole(prefs.getString("role", null));

                    newReview.setUser(user);
                    addReviewToServer(newReview);
                } else {
                    Toast.makeText(this, "Please provide a rating and comment", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Update favorite button
        updateFavoriteButton();
    }

    private void addReviewToServer(Review review) {
        ApiService apiService = RetrofitClient.getClient();
        Call<Review> call = apiService.addReview(review);
        call.enqueue(new Callback<Review>() {
            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviews.add(response.body());
                    if (reviewAdapter != null) {
                        reviewAdapter.setData(reviews);
                        reviewAdapter.notifyItemInserted(reviews.size() - 1);
                        Log.d(TAG, "New review added and adapter notified");
                    } else {
                        Log.w(TAG, "reviewAdapter is null when adding review");
                    }
                    EditText reviewCommentInput = findViewById(R.id.reviewCommentInput);
                    RatingBar ratingBar = findViewById(R.id.ratingBar);
                    if (reviewCommentInput != null) {
                        reviewCommentInput.setText("");
                    }
                    if (ratingBar != null) {
                        ratingBar.setRating(0);
                    }
                    Toast.makeText(CourseDetailActivity.this, "Review submitted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CourseDetailActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Review> call, Throwable t) {
                Log.e(TAG, "Error adding review", t);
                Toast.makeText(CourseDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPaginationButtons() {
        Log.d("CourseDetailActivity", "Setting up pagination: currentPage=" + currentPage + ", totalPages=" + totalPages);
        LinearLayout paginationContainer = findViewById(R.id.pagination_container);
        if (paginationContainer == null) {
            Log.e("CourseDetailActivity", "pagination_container is null");
            return;
        }

        // Validate currentPage
        if (currentPage < 1 || currentPage > totalPages) {
            Log.w("CourseDetailActivity", "Invalid currentPage: " + currentPage + ", resetting to 1");
            currentPage = 1;
        }

        // Clear existing dynamic buttons, but keep prev_button and next_button
        for (int i = paginationContainer.getChildCount() - 2; i > 0; i--) {
            paginationContainer.removeViewAt(i);
        }

        // Previous button
        Button prevButton = findViewById(R.id.prev_button);
        if (prevButton != null) {
            prevButton.setEnabled(currentPage > 1);
            prevButton.setOnClickListener(v -> {
                if (currentPage > 1) {
                    fetchReviews(currentPage - 1);
                }
            });
        } else {
            Log.e("CourseDetailActivity", "prev_button is null");
        }

        // Page number buttons
        int maxButtons = 5;
        int startPage = Math.max(1, currentPage - (maxButtons / 2));
        int endPage = Math.min(totalPages, startPage + maxButtons - 1);

        if (endPage - startPage + 1 < maxButtons) {
            startPage = Math.max(1, endPage - maxButtons + 1);
        }

        for (int i = startPage; i <= endPage; i++) {
            Button pageButton = new Button(this);
            pageButton.setText(String.valueOf(i));
            pageButton.setTag(i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 0, 4, 0);
            pageButton.setLayoutParams(params);
            pageButton.setBackgroundResource(R.drawable.pagination_button_background);
            pageButton.setMinWidth(0);
            pageButton.setMinimumWidth(0);
            pageButton.setPadding(12, 8, 12, 8);
            pageButton.setTextSize(14);
            pageButton.setAllCaps(false);
            pageButton.setElevation(1);

            Log.d("CourseDetailActivity", "Creating button for page: " + i + ", currentPage=" + currentPage + ", isSelected=" + (i == currentPage));
            if (i == currentPage) {
                pageButton.setEnabled(false);
                pageButton.setSelected(true);
            } else {
                pageButton.setEnabled(true);
                pageButton.setSelected(false);
                pageButton.setOnClickListener(v -> {
                    int selectedPage = (int) v.getTag();
                    fetchReviews(selectedPage);
                });
            }
            paginationContainer.addView(pageButton, paginationContainer.getChildCount() - 1);
        }

        // Next button
        Button nextButton = findViewById(R.id.next_button);
        if (nextButton != null) {
            nextButton.setEnabled(currentPage < totalPages);
            nextButton.setOnClickListener(v -> {
                if (currentPage < totalPages) {
                    fetchReviews(currentPage + 1);
                }
            });
        } else {
            Log.e("CourseDetailActivity", "next_button is null");
        }
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
                    fetchReviews(currentPage);
                    checkWishlistStatus();
                    fetchEnrollmentCount();
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