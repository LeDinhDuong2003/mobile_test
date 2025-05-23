package com.example.mobileproject;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.adapter.CommentAdapter;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.Comment;
import com.example.mobileproject.model.PaginatedCommentsResponse;
import com.example.mobileproject.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentActivity extends AppCompatActivity {
    private static final int PAGE_SIZE = 10; // Number of comments per page
    private int lessonId;
    private List<Comment> comments = new ArrayList<>();
    private CommentAdapter adapter;
    private TextView repliesCount;
    private LinearLayout paginationContainer;
    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        lessonId = getIntent().getIntExtra("lessonId", -1);
        if (lessonId == -1) {
            Toast.makeText(this, "Invalid lesson ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        repliesCount = findViewById(R.id.replies_count);
        if (repliesCount != null) {
            repliesCount.setText("0 Replies");
        }

        RecyclerView commentsRecyclerView = findViewById(R.id.recycler_comments);
        if (commentsRecyclerView != null) {
            commentsRecyclerView.setVisibility(View.GONE); // Hide RecyclerView initially
        }

        paginationContainer = findViewById(R.id.pagination_container);

        EditText commentInput = findViewById(R.id.input_comment);
        Button submitCommentButton = findViewById(R.id.btn_submit_comment);

        if (submitCommentButton != null) {
            submitCommentButton.setOnClickListener(v -> {
                String commentText = commentInput != null ? commentInput.getText().toString().trim() : "";
                if (!commentText.isEmpty()) {
                    SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                    int userId = prefs.getInt("user_id", -1);
                    if (userId == -1) {
                        Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Comment newComment = new Comment();
                    newComment.setLessonId(lessonId);
                    newComment.setUserId(userId);
                    newComment.setComment(commentText);
                    newComment.setCreatedAt(LocalDateTime.now());

                    User user = new User();
                    user.setUserId(prefs.getInt("user_id", -1));
                    user.setFullName(prefs.getString("full_name", null));
                    user.setEmail(prefs.getString("email", null));
                    user.setAvatarUrl(prefs.getString("avatar_url", null));
                    user.setPhone(prefs.getString("phone", null));
                    user.setRole(prefs.getString("role", null));

                    newComment.setUser(user);
                    addCommentToServer(newComment);
                } else {
                    Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                }
            });
        }

        fetchComments(currentPage);
    }

    private void fetchComments(int page) {
        Log.d("CommentActivity", "Fetching comments for page: " + page);
        currentPage = page; // Set currentPage before the API call
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiService apiService = RetrofitClient.getClient();
        Call<PaginatedCommentsResponse> call = apiService.getCommentsByLessonId(lessonId, page, PAGE_SIZE);
        call.enqueue(new Callback<PaginatedCommentsResponse>() {
            @Override
            public void onResponse(Call<PaginatedCommentsResponse> call, Response<PaginatedCommentsResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (response.isSuccessful() && response.body() != null) {
                    PaginatedCommentsResponse paginatedResponse = response.body();
                    Log.d("CommentActivity", "API Response: currentPage=" + paginatedResponse.getPagination().getCurrentPage() + ", totalPages=" + paginatedResponse.getPagination().getTotalPages());
                    comments.clear();
                    comments.addAll(paginatedResponse.getData());
                    currentPage = paginatedResponse.getPagination().getCurrentPage(); // Update after response
                    totalPages = paginatedResponse.getPagination().getTotalPages();
                    setupCommentsRecyclerView();
                    setupPaginationButtons();
                    if (repliesCount != null) {
                        repliesCount.setText(paginatedResponse.getPagination().getTotalItems() + " Replies");
                    }
                } else {
                    showErrorDialog("Failed to load comments. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<PaginatedCommentsResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e("CommentActivity", "Error fetching comments", t);
                showErrorDialog("Error: " + t.getMessage() + ". Please try again.");
            }
        });
    }

    private void setupCommentsRecyclerView() {
        RecyclerView commentsRecyclerView = findViewById(R.id.recycler_comments);
        if (commentsRecyclerView != null) {
            adapter = new CommentAdapter(comments);
            commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            commentsRecyclerView.setAdapter(adapter);
            commentsRecyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged(); // Ensure the RecyclerView refreshes
        }
    }

    private void setupPaginationButtons() {
        Log.d("CommentActivity", "Setting up pagination: currentPage=" + currentPage + ", totalPages=" + totalPages);
        LinearLayout paginationContainer = findViewById(R.id.pagination_container);
        if (paginationContainer == null) {
            Log.e("CommentActivity", "pagination_container is null");
            return;
        }

        // Validate currentPage
        if (currentPage < 1 || currentPage > totalPages) {
            Log.w("CommentActivity", "Invalid currentPage: " + currentPage + ", resetting to 1");
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
                    fetchComments(currentPage - 1);
                }
            });
        } else {
            Log.e("CommentActivity", "prev_button is null");
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

            Log.d("CommentActivity", "Creating button for page: " + i + ", currentPage=" + currentPage + ", isSelected=" + (i == currentPage));
            if (i == currentPage) {
                pageButton.setEnabled(false);
                pageButton.setSelected(true);
            } else {
                pageButton.setEnabled(true);
                pageButton.setSelected(false);
                pageButton.setOnClickListener(v -> {
                    int selectedPage = (int) v.getTag();
                    fetchComments(selectedPage);
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
                    fetchComments(currentPage + 1);
                }
            });
        } else {
            Log.e("CommentActivity", "next_button is null");
        }
    }

    private void addCommentToServer(Comment comment) {
        ApiService apiService = RetrofitClient.getClient();
        Call<Comment> call = apiService.addComment(comment);
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EditText commentInput = findViewById(R.id.input_comment);
                    if (commentInput != null) {
                        commentInput.setText("");
                    }
                    fetchComments(currentPage);
                    Toast.makeText(CommentActivity.this, "Comment submitted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CommentActivity.this, "Failed to submit comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Log.e("CommentActivity", "Error adding comment", t);
                Toast.makeText(CommentActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> fetchComments(currentPage))
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
    }
}