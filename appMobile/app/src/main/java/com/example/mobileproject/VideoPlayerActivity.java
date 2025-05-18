package com.example.mobileproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.adapter.LessonAdapter;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.Lesson;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity {
    private int lessonId;
    private int courseId;
    private String videoUrl;
    private List<Lesson> lessons = new ArrayList<>();
    private RecyclerView lessonRecyclerView;
    private boolean isEnrolled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        lessonId = getIntent().getIntExtra("lessonId", -1);
        courseId = getIntent().getIntExtra("courseId", -1);

        if (lessonId == -1 || courseId == -1) {
            Log.e("VideoPlayerActivity", "Invalid lessonId or courseId");
            Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        VideoView videoView = findViewById(R.id.videoView);
        if (videoView == null) {
            Log.e("VideoPlayerActivity", "videoView not found");
            Toast.makeText(this, "Error initializing video player", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        LinearLayout bottomSheet = findViewById(R.id.bottomSheet);
        if (bottomSheet == null) {
            Log.e("VideoPlayerActivity", "bottomSheet not found");
            Toast.makeText(this, "Error initializing lesson list", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        lessonRecyclerView = findViewById(R.id.lessonRecyclerView);
        if (lessonRecyclerView == null) {
            Log.e("VideoPlayerActivity", "lessonRecyclerView not found");
            Toast.makeText(this, "Error initializing lesson list", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        lessonRecyclerView.setVisibility(View.GONE); // Ẩn RecyclerView ban đầu

        checkEnrollmentStatus();
        fetchLessonData();
        fetchLessonsForCourse();
    }

    private void checkEnrollmentStatus() {
        Integer userId = MockAuthManager.getInstance().getCurrentUserId();
        if (userId == null) {
            Log.e("VideoPlayerActivity", "No user data available");
            Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            isEnrolled = false;
            setupButtons();
            return;
        }

        ApiService apiService = RetrofitClient.getClient();
        Call<Boolean> call = apiService.checkEnrollment(courseId, userId);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isEnrolled = response.body();
                    setupButtons();
                } else {
                    isEnrolled = false;
                    setupButtons();
                    showErrorDialog("Failed to check enrollment status. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("VideoPlayerActivity", "Error checking enrollment", t);
                isEnrolled = false;
                setupButtons();
                showErrorDialog("Error: " + t.getMessage() + ". Please try again.");
            }
        });
    }

    private void setupButtons() {
        Button favoriteButton = findViewById(R.id.favoriteButton);
        Button actionButton = findViewById(R.id.actionButton);

        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v -> Toast.makeText(this, "Add to favorites", Toast.LENGTH_SHORT).show());
        }

        if (actionButton != null) {
            if (isEnrolled) {
                actionButton.setText("Add Comment");
                actionButton.setOnClickListener(v -> {
                    Intent intent = new Intent(VideoPlayerActivity.this, CommentActivity.class);
                    intent.putExtra("lessonId", lessonId);
                    startActivity(intent);
                });
            } else {
                actionButton.setText("Buy Now");
                actionButton.setOnClickListener(v -> Toast.makeText(this, "Redirect to purchase page", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void fetchLessonData() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiService apiService = RetrofitClient.getClient();
        Call<Lesson> call = apiService.getLessonById(lessonId);
        call.enqueue(new Callback<Lesson>() {
            @Override
            public void onResponse(Call<Lesson> call, Response<Lesson> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (response.isSuccessful() && response.body() != null) {
                    Lesson lesson = response.body();
                    videoUrl = lesson.getVideoUrl();
                    if (videoUrl != null && !videoUrl.isEmpty()) {
                        try {
                            VideoView videoView = findViewById(R.id.videoView);
                            videoView.setVideoURI(Uri.parse(videoUrl));
                            videoView.setOnPreparedListener(mp -> videoView.start());
                            videoView.setOnErrorListener((mp, what, extra) -> {
                                Log.e("VideoPlayerActivity", "Video playback error: what=" + what + ", extra=" + extra);
                                Toast.makeText(VideoPlayerActivity.this, "Error playing video", Toast.LENGTH_SHORT).show();
                                return true;
                            });
                        } catch (Exception e) {
                            Log.e("VideoPlayerActivity", "Error setting video URI", e);
                            showErrorDialog("Invalid video URL. Please try again.");
                        }
                    } else {
                        showErrorDialog("No video URL available. Please try again.");
                    }
                } else {
                    showErrorDialog("Failed to load lesson data. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<Lesson> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e("VideoPlayerActivity", "Error fetching lesson data", t);
                showErrorDialog("Error: " + t.getMessage() + ". Please try again.");
            }
        });
    }

    private void fetchLessonsForCourse() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiService apiService = RetrofitClient.getClient();
        Call<List<Lesson>> call = apiService.getLessonsByCourseId(courseId);
        call.enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (response.isSuccessful() && response.body() != null) {
                    lessons.clear();
                    lessons.addAll(response.body());
                    setupLessonRecyclerView();
                } else {
                    showErrorDialog("Failed to load lessons. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<List<Lesson>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e("VideoPlayerActivity", "Error fetching lessons", t);
                showErrorDialog("Error: " + t.getMessage() + ". Please try again.");
            }
        });
    }

    private void setupLessonRecyclerView() {
        lessonRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LessonAdapter adapter = new LessonAdapter(lessons, LessonAdapter.TYPE_PAGE_2, lesson -> {
            lessonId = lesson.getLessonId();
            videoUrl = lesson.getVideoUrl();
            if (videoUrl != null && !videoUrl.isEmpty()) {
                try {
                    VideoView videoView = findViewById(R.id.videoView);
                    videoView.setVideoURI(Uri.parse(videoUrl));
                    videoView.start();
                } catch (Exception e) {
                    Toast.makeText(VideoPlayerActivity.this, "Error switching video", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(VideoPlayerActivity.this, "No video available", Toast.LENGTH_SHORT).show();
            }
        });
        lessonRecyclerView.setAdapter(adapter);
        lessonRecyclerView.setVisibility(View.VISIBLE); // Hiển thị sau khi gán adapter
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> {
                    if (message.contains("lesson data")) {
                        fetchLessonData();
                    } else if (message.contains("lessons")) {
                        fetchLessonsForCourse();
                    } else {
                        checkEnrollmentStatus();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
    }
}