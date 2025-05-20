package com.example.mobileproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.adapter.ScoreAdapter;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.PagedResponse;
import com.example.mobileproject.model.Score;
import com.example.mobileproject.model.ScoreResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class ScoreActivity extends AppCompatActivity {

    private static final String TAG = "ScoreActivity";
    private static final int PAGE_SIZE = 5;
    private static final long LOAD_MORE_DELAY = 500;
    private int USER_ID;
    private SharedPreferences sharedPreferences;
    private ImageButton backButton;
    private RecyclerView scoreRecyclerView;
    private ScoreAdapter scoreAdapter;
    private TextView tvTitle;
    private TextView tvNoCourses;
    private FrameLayout initialLoadingLayout;
    private FrameLayout loadMoreLayout;
    private EditText searchEditText;
    private CardView btnSearch;

    private List<Score> displayedScores = new ArrayList<>();
    private String searchQuery = null;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lichsulamquiz);
        sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        USER_ID = sharedPreferences.getInt("user_id", 1);
        initUI();
        setupBackButton();
        setupScoreRecyclerView();
        setupSearch();
        resetAndLoadScores();
    }

    private void initUI() {
        backButton = findViewById(R.id.backButton);
        scoreRecyclerView = findViewById(R.id.scoreRecyclerView);
        tvTitle = findViewById(R.id.tvTitle);
        tvNoCourses = findViewById(R.id.tvNoCourses);
        initialLoadingLayout = findViewById(R.id.initialLoadingLayout);
        loadMoreLayout = findViewById(R.id.loadMoreLayout);
        searchEditText = findViewById(R.id.searchEditText);
        btnSearch = findViewById(R.id.btnSearch);
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> finish());
    }

    private void setupSearch() {
        btnSearch.setOnClickListener(v -> {
            searchQuery = searchEditText.getText().toString().trim();
            if (searchQuery.isEmpty()) {
                searchQuery = null;
            }
            resetAndLoadScores();
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                searchQuery = searchEditText.getText().toString().trim();
                if (searchQuery.isEmpty()) {
                    searchQuery = null;
                }
                resetAndLoadScores();
                return true;
            }
            return false;
        });
    }

    private void setupScoreRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        scoreRecyclerView.setLayoutManager(layoutManager);

        scoreAdapter = new ScoreAdapter(this, displayedScores);
        scoreAdapter.setOnItemClickListener(score -> {
            Toast.makeText(this, "Clicked: " + score.getCourseTitle(), Toast.LENGTH_SHORT).show();
        });
        scoreAdapter.setOnRetryClickListener(score -> {
            Toast.makeText(this, "Retry: " + score.getCourseTitle(), Toast.LENGTH_SHORT).show();
        });
        scoreRecyclerView.setAdapter(scoreAdapter);

        scoreRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMorePages) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 1
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= PAGE_SIZE) {
                            loadMoreScores();
                        }
                    }
                }
            }
        });
    }

    private void resetAndLoadScores() {
        currentPage = 0;
        displayedScores.clear();
        scoreAdapter.notifyDataSetChanged();
        hasMorePages = true;

        tvNoCourses.setVisibility(View.GONE);
        updateTitle();
        showInitialLoadingView();
        fetchScores();
    }

    private void updateTitle() {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            tvTitle.setText("Kết quả tìm kiếm: " + searchQuery);
        } else {
            tvTitle.setText("Lịch sử làm bài");
        }
    }

    private void showInitialLoadingView() {
        initialLoadingLayout.setVisibility(View.VISIBLE);
        scoreRecyclerView.setVisibility(View.GONE);
        loadMoreLayout.setVisibility(View.GONE);
    }

    private void hideInitialLoadingView() {
        initialLoadingLayout.setVisibility(View.GONE);
        scoreRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoadMoreView() {
        loadMoreLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoadMoreView() {
        loadMoreLayout.setVisibility(View.GONE);
    }

    private void loadMoreScores() {
        if (isLoading || !hasMorePages) return;

        isLoading = true;
        showLoadMoreView();
        handler.postDelayed(this::fetchScores, LOAD_MORE_DELAY);
    }

    private void fetchScores() {
        RetrofitClient.getClient().getScores(USER_ID, currentPage, PAGE_SIZE, searchQuery)
                .enqueue(new Callback<PagedResponse<ScoreResponse>>() {
                    @Override
                    public void onResponse(Call<PagedResponse<ScoreResponse>> call, Response<PagedResponse<ScoreResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PagedResponse<ScoreResponse> pagedResponse = response.body();

                            List<Score> newScores = new ArrayList<>();
                            for (ScoreResponse scoreResponse : pagedResponse.getItems()) {
                                newScores.add(scoreResponse.toScore());
                            }

                            int insertPosition = displayedScores.size();
                            displayedScores.addAll(newScores);
                            scoreAdapter.notifyItemRangeInserted(insertPosition, newScores.size());

                            currentPage++;
                            hasMorePages = pagedResponse.hasMorePages();

                            if (displayedScores.isEmpty()) {
                                tvNoCourses.setVisibility(View.VISIBLE);
                                scoreRecyclerView.setVisibility(View.GONE);
                            } else {
                                tvNoCourses.setVisibility(View.GONE);
                                scoreRecyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(ScoreActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                        }

                        isLoading = false;
                        hideInitialLoadingView();
                        hideLoadMoreView();
                    }

                    @Override
                    public void onFailure(Call<PagedResponse<ScoreResponse>> call, Throwable t) {
                        Toast.makeText(ScoreActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        isLoading = false;
                        hideInitialLoadingView();
                        hideLoadMoreView();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}