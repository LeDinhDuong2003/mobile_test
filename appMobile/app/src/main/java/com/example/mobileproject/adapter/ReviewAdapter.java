package com.example.mobileproject.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.model.Review;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private static final String TAG = "ReviewAdapter";
    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        Log.d(TAG, "ReviewAdapter initialized with " + this.reviews.size() + " reviews");
    }

    public void setData(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        Log.d(TAG, "setData called with " + this.reviews.size() + " reviews");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Binding review at position: " + position);
        Review review = reviews.get(position);
        if (holder.username != null) {
            holder.username.setText(review.getUser() != null && review.getUser().getFullName() != null
                    ? review.getUser().getFullName() : "Anonymous");
        }
        if (holder.ratingBar != null) {
            holder.ratingBar.setRating(review.getRating() != null ? review.getRating() : 0);
        }
        if (holder.comment != null) {
            holder.comment.setText(review.getComment() != null ? review.getComment() : "");
        }
        if (holder.time != null) {
            holder.time.setText(review.getCreatedAt() != null
                    ? review.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        }
    }

    @Override
    public int getItemCount() {
        int size = reviews.size();
        Log.d(TAG, "getItemCount: " + size);
        return size;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, comment, time;
        RatingBar ratingBar;

        public ViewHolder(View view) {
            super(view);
            username = view.findViewById(R.id.txt_username);
            ratingBar = view.findViewById(R.id.ratingBar);
            comment = view.findViewById(R.id.txt_comment);
            time = view.findViewById(R.id.txt_time);
        }
    }
}