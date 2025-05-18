package com.example.mobileproject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    private int lessonId;
    private List<Comment> comments = new ArrayList<>();
    private CommentAdapter adapter;
    private TextView repliesCount;

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
            commentsRecyclerView.setVisibility(View.GONE); // Ẩn RecyclerView ban đầu
        }

        EditText commentInput = findViewById(R.id.input_comment);
        Button submitCommentButton = findViewById(R.id.btn_submit_comment);

        if (submitCommentButton != null) {
            submitCommentButton.setOnClickListener(v -> {
                String commentText = commentInput != null ? commentInput.getText().toString().trim() : "";
                if (!commentText.isEmpty()) {
                    Integer userId = MockAuthManager.getInstance().getCurrentUserId();
                    if (userId == null) {
                        Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Comment newComment = new Comment();
                    newComment.setLessonId(lessonId);
                    newComment.setUserId(userId);
                    newComment.setComment(commentText);
                    newComment.setCreatedAt(LocalDateTime.now());
                    newComment.setUser(MockAuthManager.getInstance().getCurrentUser());
                    addCommentToServer(newComment);
                } else {
                    Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                }
            });
        }

        fetchComments();
    }

    private void fetchComments() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiService apiService = RetrofitClient.getClient();
        Call<List<Comment>> call = apiService.getCommentsByLessonId(lessonId);
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (response.isSuccessful() && response.body() != null) {
                    comments.clear();
                    comments.addAll(response.body());
                    setupCommentsRecyclerView();
                    if (repliesCount != null) {
                        repliesCount.setText(comments.size() + " Replies");
                    }
                } else {
                    showErrorDialog("Failed to load comments. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
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
            commentsRecyclerView.setVisibility(View.VISIBLE); // Hiển thị sau khi gán adapter
        }
    }

    private void addCommentToServer(Comment comment) {
        ApiService apiService = RetrofitClient.getClient();
//        Call<Comment> call = apiService.addComment(comment);
//        call.enqueue(new Callback<Comment>() {
//            @Override
//            public void onResponse(Call<Comment> call, Response<Comment> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    comments.add(response.body());
//                    if (adapter != null) {
//                        adapter.notifyItemInserted(comments.size() - 1);
//                    }
//                    EditText commentInput = findViewById(R.id.input_comment);
//                    if (commentInput != null) {
//                        commentInput.setText("");
//                    }
//                    if (repliesCount != null) {
//                        repliesCount.setText(comments.size() + " Replies");
//                    }
//                    Toast.makeText(CommentActivity.this, "Comment submitted", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(CommentActivity.this, "Failed to submit comment", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Comment> call, Throwable t) {
//                Log.e("CommentActivity", "Error adding comment", t);
//                Toast.makeText(CommentActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> fetchComments())
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
    }
}