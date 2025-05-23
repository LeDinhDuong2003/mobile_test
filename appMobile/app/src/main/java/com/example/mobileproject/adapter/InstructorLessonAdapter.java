package com.example.mobileproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mobileproject.R;
import com.example.mobileproject.model.Lesson;

import java.util.List;

public class InstructorLessonAdapter extends RecyclerView.Adapter<InstructorLessonAdapter.LessonViewHolder> {

    private final Context context;
    private final List<Lesson> lessonList;
    private OnLessonActionListener listener;

    public interface OnLessonActionListener {
        void onEditLesson(Lesson lesson);
        void onDeleteLesson(Lesson lesson);
    }

    public InstructorLessonAdapter(Context context, List<Lesson> lessonList, OnLessonActionListener listener) {
        this.context = context;
        this.lessonList = lessonList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instructor_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);

        // Load video thumbnail (simplified)
        Glide.with(context)
                .load(lesson.getVideoUrl())
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image))
                .into(holder.lessonThumbnail);

        // Set lesson info
        holder.lessonTitle.setText(lesson.getTitle());
        holder.lessonPosition.setText("Bài " + lesson.getPosition());
        holder.lessonDuration.setText(formatDuration(lesson.getDuration()));
        holder.lessonVideoUrl.setText(lesson.getVideoUrl());

        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditLesson(lesson);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteLesson(lesson);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds == 0) return "0:00";
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        ImageView lessonThumbnail;
        TextView lessonTitle, lessonPosition, lessonDuration, lessonVideoUrl;
        ImageView btnEdit, btnDelete;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            lessonThumbnail = itemView.findViewById(R.id.lessonThumbnail);
            lessonTitle = itemView.findViewById(R.id.lessonTitle);
            lessonPosition = itemView.findViewById(R.id.lessonPosition);
            lessonDuration = itemView.findViewById(R.id.lessonDuration);
            lessonVideoUrl = itemView.findViewById(R.id.lessonVideoUrl);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}