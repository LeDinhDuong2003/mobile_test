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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.adapter.LessonAdapter;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.Lesson;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayerActivity";
    private int lessonId;
    private int courseId;
    private String videoUrl;
    private List<Lesson> lessons = new ArrayList<>();
    private RecyclerView lessonRecyclerView;
    private boolean isEnrolled = true; // Mặc định cho phép xem
    private PlayerView playerView;
    private ExoPlayer player;
    private ProgressBar progressBar;

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

        ImageView backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Khởi tạo ExoPlayer
        playerView = findViewById(R.id.playerView);
        if (playerView == null) {
            Log.e(TAG, "Không tìm thấy playerView");
            Toast.makeText(this, "Lỗi khởi tạo trình phát video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Khởi tạo ExoPlayer
        initializePlayer();

        // Cấu hình BottomSheet
        LinearLayout bottomSheet = findViewById(R.id.bottomSheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        lessonRecyclerView = findViewById(R.id.lessonRecyclerView);
        if (lessonRecyclerView != null) {
            lessonRecyclerView.setVisibility(View.GONE);
        }

        // Thiết lập các nút
        setupButtons();

        // Tải dữ liệu bài học và danh sách bài học
        fetchLessonData();
        fetchLessonsForCourse();
    }

    private void initializePlayer() {
        // Tạo ExoPlayer instance
        player = new ExoPlayer.Builder(this).build();

        // Gán player vào PlayerView
        playerView.setPlayer(player);

        // Thiết lập listener
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    // Video đã sẵn sàng phát
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                } else if (state == Player.STATE_BUFFERING) {
                    // Video đang buffer
                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                } else if (state == Player.STATE_ENDED) {
                    // Video đã kết thúc - có thể chuyển đến bài tiếp theo
                    Log.d(TAG, "Phát video kết thúc - có thể chuyển đến bài tiếp theo");
                    playNextLessonIfAvailable();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                // Lỗi phát video
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Lỗi phát video: " + error.getMessage(), error);
                Toast.makeText(VideoPlayerActivity.this,
                        "Lỗi phát video: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                // Thử phát một video mẫu nếu video chính bị lỗi
                playVideoWithUrl("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4");
            }
        });
    }

    private void playNextLessonIfAvailable() {
        // Tìm bài học hiện tại trong danh sách
        int currentIndex = -1;
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getLessonId() == lessonId) {
                currentIndex = i;
                break;
            }
        }

        // Nếu có bài học tiếp theo, phát nó
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
        if (player == null) {
            return;
        }

        try {
            Log.d(TAG, "Đang phát video từ URL: " + url);

            // Tạo đối tượng MediaItem từ URL
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));

            // Đặt MediaItem vào Player và bắt đầu phát
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
        Button actionButton = findViewById(R.id.actionButton);

        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v ->
                    Toast.makeText(this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show());
        }

        if (actionButton != null) {
            if (isEnrolled) {
                actionButton.setText("Bình luận");
                actionButton.setOnClickListener(v -> {
                    Intent intent = new Intent(VideoPlayerActivity.this, CommentActivity.class);
                    intent.putExtra("lessonId", lessonId);
                    startActivity(intent);
                });
            } else {
                actionButton.setText("Mua khóa học");
                actionButton.setOnClickListener(v ->
                        Toast.makeText(this, "Chuyển đến trang thanh toán", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void fetchLessonData() {
        Log.d(TAG, "Đang lấy thông tin bài học, lessonId: " + lessonId);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Thử phát một video mẫu trước khi gọi API để đảm bảo ExoPlayer hoạt động
        playVideoWithUrl("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4");

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
                        // Giữ nguyên video mẫu đang phát
                    }
                } else {
                    Log.e(TAG, "Lỗi API: " + response.code());
                    // Giữ nguyên video mẫu đang phát
                    Toast.makeText(VideoPlayerActivity.this,
                            "Không thể lấy dữ liệu video từ server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Lesson> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Lỗi kết nối API: " + t.getMessage(), t);
                // Giữ nguyên video mẫu đang phát
                Toast.makeText(VideoPlayerActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Log.d(TAG, "Đã nhận được " + lessons.size() + " bài học");
                    setupLessonRecyclerView();
                } else {
                    Log.e(TAG, "Lỗi API: " + response.code());
                    lessons = createMockLessons();
                    Log.d(TAG, "Sử dụng danh sách bài học mẫu");
                    setupLessonRecyclerView();
                }
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

        // Danh sách các URL video mẫu từ ExoPlayer
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
            lesson.setVideoUrl(sampleUrls[(i-1) % sampleUrls.length]);
            lesson.setPosition(i);
            lesson.setDuration(i * 300); // 5-25 phút
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
        return titles[(index-1) % titles.length];
    }

    private void setupLessonRecyclerView() {
        lessonRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LessonAdapter adapter = new LessonAdapter(lessons, LessonAdapter.TYPE_PAGE_2, lesson -> {
            // Khi người dùng nhấn vào một bài học trong danh sách
            lessonId = lesson.getLessonId();
            videoUrl = lesson.getVideoUrl();

            Log.d(TAG, "Đã chọn bài học: " + lesson.getTitle() + ", videoUrl: " + videoUrl);

            if (videoUrl != null && !videoUrl.isEmpty()) {
                // Hiển thị tiêu đề bài học đang phát
                Toast.makeText(this, "Đang phát: " + lesson.getTitle(), Toast.LENGTH_SHORT).show();
                playVideoWithUrl(videoUrl);
            } else {
                // Sử dụng video mẫu nếu không có URL thực
                playVideoWithUrl("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4");
                Toast.makeText(this, "Đang phát video mẫu cho: " + lesson.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        lessonRecyclerView.setAdapter(adapter);
        lessonRecyclerView.setVisibility(View.VISIBLE);
        Log.d(TAG, "Đã thiết lập RecyclerView cho danh sách bài học");
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