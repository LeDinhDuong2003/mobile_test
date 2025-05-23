package com.example.mobileproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobileproject.adapter.LessonAdapter;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.Enrollment;
import com.example.mobileproject.model.Lesson;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoPlayerActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayerActivity";
    private int lessonId;
    private int courseId;
    private String videoUrl;
    private List<Lesson> lessons = new ArrayList<>();
    private RecyclerView lessonRecyclerView;
    private boolean isEnrolled;
    private PlayerView playerView;
    private ExoPlayer player;
    private ProgressBar progressBar;
    private Button enrollButton;
    private Button actionButton;
    private Button quizButton; // Thêm biến cho quizButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        lessonId = getIntent().getIntExtra("lessonId", -1);
        courseId = getIntent().getIntExtra("courseId", -1);

        Log.d(TAG, "Khởi tạo VideoPlayerActivity: lessonId=" + lessonId + ", courseId=" + courseId);

        if (lessonId == -1 || courseId == -1) {
            Log.e(TAG, "Không có lessonId hoặc courseId hợp lệ");
            Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo các view
        ImageView backButton = findViewById(R.id.back_button);
        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progressBar);
        lessonRecyclerView = findViewById(R.id.lessonRecyclerView);
        enrollButton = findViewById(R.id.enrollButton);
        actionButton = findViewById(R.id.actionButton);
        quizButton = findViewById(R.id.quizButton); // Khởi tạo quizButton

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (playerView == null) {
            Log.e(TAG, "Không tìm thấy playerView");
            Toast.makeText(this, "Lỗi khởi tạo trình phát video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (lessonRecyclerView != null) {
            lessonRecyclerView.setVisibility(View.GONE);
        }

        // Khởi tạo ExoPlayer
        initializePlayer();

        // Thiết lập các nút
        setupButtons();

        // Tải dữ liệu bài học và danh sách bài học
        checkEnrollmentStatus();
        fetchLessonData();
        fetchLessonsForCourse();
    }

    private void checkEnrollmentStatus() {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            isEnrolled = false;
            updateEnrollButtonVisibility();
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
                    setupButtons();
                } else {
                    isEnrolled = false;
                    updateEnrollButtonVisibility();
                    setupButtons();
                    Toast.makeText(VideoPlayerActivity.this, "Failed to check enrollment status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Error checking enrollment", t);
                isEnrolled = false;
                updateEnrollButtonVisibility();
                setupButtons();
                Toast.makeText(VideoPlayerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                } else if (state == Player.STATE_BUFFERING) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                } else if (state == Player.STATE_ENDED) {
                    Log.d(TAG, "Phát video kết thúc - chuyển đến bài tiếp theo");
                    playNextLessonIfAvailable();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Lỗi phát video: " + error.getMessage(), error);
                Toast.makeText(VideoPlayerActivity.this, "Lỗi phát video: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                playVideoWithUrl("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4");
            }
        });
    }

    private void playNextLessonIfAvailable() {
        int currentIndex = -1;
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getLessonId() == lessonId) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex >= 0 && currentIndex < lessons.size() - 1) {
            Lesson nextLesson = lessons.get(currentIndex + 1);
            lessonId = nextLesson.getLessonId();
            videoUrl = nextLesson.getVideoUrl();

            Log.d(TAG, "Tự động chuyển đến bài học tiếp theo: " + nextLesson.getTitle());
            if (videoUrl != null && !videoUrl.isEmpty()) {
                playVideoWithUrl(videoUrl);
            }
        }
    }

    private void playVideoWithUrl(String url) {
        if (player == null) return;

        try {
            Log.d(TAG, "Đang phát video từ URL: " + url);
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi cài đặt URL video", e);
            Toast.makeText(this, "Lỗi phát video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtons() {
        Button favoriteButton = findViewById(R.id.favoriteButton);
        enrollButton = findViewById(R.id.enrollButton);
        actionButton = findViewById(R.id.actionButton);
        quizButton = findViewById(R.id.quizButton); // Khởi tạo quizButton

        if (favoriteButton != null) {
            favoriteButton.setVisibility(View.VISIBLE);
            favoriteButton.setOnClickListener(v ->
                    Toast.makeText(this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show());
        }

        if (isEnrolled) {
            if (actionButton != null) {
                actionButton.setText("Bình luận");
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setOnClickListener(v -> {
                    Intent intent = new Intent(VideoPlayerActivity.this, CommentActivity.class);
                    intent.putExtra("lessonId", lessonId);
                    startActivity(intent);
                });
            }
            if (quizButton != null) {
                quizButton.setVisibility(View.VISIBLE);
                quizButton.setOnClickListener(v -> {
                    Intent intent = new Intent(VideoPlayerActivity.this, CountdownActivity.class);
                    intent.putExtra("lession_id", lessonId);
                    startActivity(intent);
                });
            }
            if (enrollButton != null) {
                enrollButton.setVisibility(View.GONE);
            }
        } else {
            if (actionButton != null) {
                actionButton.setVisibility(View.GONE);
            }
            if (quizButton != null) {
                quizButton.setVisibility(View.GONE);
            }
            if (enrollButton != null) {
                enrollButton.setVisibility(View.VISIBLE);
                enrollButton.setOnClickListener(v -> enrollInCourse());
            }
        }
    }

    private void enrollInCourse() {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        enrollButton.setEnabled(false);
        ApiService apiService = RetrofitClient.getClient();
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("user_id", userId);
        Call<Enrollment> call = apiService.enrollInCourse(courseId, requestBody);
        call.enqueue(new Callback<Enrollment>() {
            @Override
            public void onResponse(Call<Enrollment> call, Response<Enrollment> response) {
                enrollButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    isEnrolled = true;
                    updateEnrollButtonVisibility();
                    setupButtons();
                    Toast.makeText(VideoPlayerActivity.this, "Successfully enrolled in the course!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VideoPlayerActivity.this, "Failed to enroll. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Enrollment> call, Throwable t) {
                enrollButton.setEnabled(true);
                Log.e(TAG, "Error enrolling in course", t);
                Toast.makeText(VideoPlayerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEnrollButtonVisibility() {
        if (enrollButton != null) {
            enrollButton.setVisibility(isEnrolled ? View.GONE : View.VISIBLE);
        }
        if (actionButton != null) {
            actionButton.setVisibility(isEnrolled ? View.VISIBLE : View.GONE);
        }
        if (quizButton != null) {
            quizButton.setVisibility(isEnrolled ? View.VISIBLE : View.GONE);
        }
    }

    private void fetchLessonData() {
        Log.d(TAG, "Đang lấy thông tin bài học, lessonId: " + lessonId);
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
                    Log.d(TAG, "Đã nhận được URL video từ API: " + videoUrl);

                    if (videoUrl != null && !videoUrl.isEmpty()) {
                        playVideoWithUrl(videoUrl);
                    } else {
                        Log.e(TAG, "URL video từ API là null hoặc rỗng");
                        playVideoWithUrl("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4");
                    }
                } else {
                    Log.e(TAG, "Lỗi API: " + response.code());
                    playVideoWithUrl("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4");
                    Toast.makeText(VideoPlayerActivity.this, "Không thể lấy dữ liệu video từ server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Lesson> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Lỗi kết nối API: " + t.getMessage(), t);
                playVideoWithUrl("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4");
                Toast.makeText(VideoPlayerActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLessonsForCourse() {
        Log.d(TAG, "Đang lấy danh sách bài học cho khóa học: " + courseId);
        ApiService apiService = RetrofitClient.getClient();
        Call<List<Lesson>> call = apiService.getLessonsByCourseId(courseId);
        call.enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lessons.clear();
                    lessons.addAll(response.body());
                    Log.d(TAG, "Đã nhận được " + lessons.size() + " bài học từ API");
                } else {
                    Log.e(TAG, "Lỗi API: " + response.code());
                    lessons = createMockLessons();
                    Log.d(TAG, "Sử dụng danh sách bài học mẫu do API thất bại");
                }
                setupLessonRecyclerView();
            }

            @Override
            public void onFailure(Call<List<Lesson>> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối API: " + t.getMessage(), t);
                lessons = createMockLessons();
                Log.d(TAG, "Sử dụng danh sách bài học mẫu do lỗi kết nối");
                setupLessonRecyclerView();
            }
        });
    }

    private List<Lesson> createMockLessons() {
        List<Lesson> mockLessons = new ArrayList<>();
        String[] sampleUrls = {
                "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                "https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3",
                "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
                "https://storage.googleapis.com/exoplayer-test-media-1/mp4/frame-counter-one-hour.mp4",
                "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"
        };

        for (int i = 1; i <= 5; i++) {
            Lesson lesson = new Lesson();
            lesson.setLessonId(i);
            lesson.setCourseId(courseId);
            lesson.setTitle("Bài học " + i + ": " + getSampleTitle(i));
            lesson.setVideoUrl(sampleUrls[(i - 1) % sampleUrls.length]);
            lesson.setPosition(i);
            lesson.setDuration(i * 300);
            mockLessons.add(lesson);
        }
        return mockLessons;
    }

    private String getSampleTitle(int index) {
        String[] titles = {
                "Giới thiệu khóa học",
                "Cơ bản về lập trình Android",
                "Xây dựng layout với XML",
                "Thao tác với cơ sở dữ liệu SQLite",
                "Tương tác với API và mạng"
        };
        return titles[(index - 1) % titles.length];
    }

    private void setupLessonRecyclerView() {
        if (lessonRecyclerView == null) return;

        lessonRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LessonAdapter adapter = new LessonAdapter(lessons, LessonAdapter.TYPE_PAGE_2, lesson -> {
            lessonId = lesson.getLessonId();
            videoUrl = lesson.getVideoUrl();
            Log.d(TAG, "Đã chọn bài học: " + lesson.getTitle() + ", videoUrl: " + videoUrl);
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Toast.makeText(this, "Đang phát: " + lesson.getTitle(), Toast.LENGTH_SHORT).show();
                playVideoWithUrl(videoUrl);
            } else {
                playVideoWithUrl("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4");
                Toast.makeText(this, "Đang phát video mẫu cho: " + lesson.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        lessonRecyclerView.setAdapter(adapter);
        lessonRecyclerView.setVisibility(View.VISIBLE);
        Log.d(TAG, "Đã thiết lập RecyclerView với " + lessons.size() + " bài học");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}