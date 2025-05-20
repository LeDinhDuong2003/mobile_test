package com.example.mobileproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mobileproject.R;
import com.example.mobileproject.model.Score;

import java.util.List;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private final Context context;
    private final List<Score> scoreList;
    private OnItemClickListener itemClickListener;
    private OnRetryClickListener retryClickListener;

    public interface OnItemClickListener {
        void onItemClick(Score score);
    }

    public interface OnRetryClickListener {
        void onRetryClick(Score score);
    }

    public ScoreAdapter(Context context, List<Score> scoreList) {
        this.context = context;
        this.scoreList = scoreList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnRetryClickListener(OnRetryClickListener listener) {
        this.retryClickListener = listener;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        Score score = scoreList.get(position);

        Glide.with(context)
                .load(score.getCourseUrl())
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image))
                .into(holder.courseImage);

        holder.courseTitle.setText(score.getCourseTitle());
        holder.lessonName.setText(score.getLessonTitle());
        holder.ratingText.setText("Số điểm: " + score.getScore());
        holder.dateCompleted.setText("Ngày làm: " + score.getNgaylambai());

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(score);
            }
        });

        holder.retryButton.setOnClickListener(v -> {
            if (retryClickListener != null) {
                retryClickListener.onRetryClick(score);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scoreList.size();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        ImageView courseImage;
        TextView courseTitle, lessonName, ratingText, dateCompleted;
        Button retryButton;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            courseImage = itemView.findViewById(R.id.courseImage);
            courseTitle = itemView.findViewById(R.id.courseTitle);
            lessonName = itemView.findViewById(R.id.lessonName);
            ratingText = itemView.findViewById(R.id.ratingText);
            dateCompleted = itemView.findViewById(R.id.dateCompleted);
            retryButton = itemView.findViewById(R.id.retryButton);
        }
    }
}