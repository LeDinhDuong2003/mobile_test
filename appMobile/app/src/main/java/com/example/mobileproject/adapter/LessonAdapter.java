package com.example.mobileproject.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobileproject.R;
import com.example.mobileproject.model.Lesson;

import java.util.ArrayList;
import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "LessonAdapter";
    private List<Lesson> lessons;
    public static final int TYPE_PAGE_1 = 1;
    public static final int TYPE_PAGE_2 = 2;
    private int layoutType;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public LessonAdapter(List<Lesson> lessons, int layoutType, OnLessonClickListener listener) {
        this.lessons = lessons != null ? lessons : new ArrayList<>();
        this.layoutType = layoutType;
        this.listener = listener;
        Log.d(TAG, "LessonAdapter initialized with " + this.lessons.size() + " lessons");
    }

    @Override
    public int getItemViewType(int position) {
        return layoutType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_PAGE_1) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
            return new LessonViewHolderPage1(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson_playing, parent, false);
            return new LessonViewHolderPage2(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "Binding lesson at position: " + position);
        Lesson lesson = lessons.get(position);
        if (holder instanceof LessonViewHolderPage1) {
            LessonViewHolderPage1 page1Holder = (LessonViewHolderPage1) holder;
            if (page1Holder.title != null) {
                page1Holder.title.setText(lesson.getTitle() != null ? lesson.getTitle() : "");
            }
            if (page1Holder.lessonNumber != null) {
                page1Holder.lessonNumber.setText("Lesson " + (lesson.getPosition() != null ? lesson.getPosition() : ""));
            }
            if (page1Holder.thumbnail != null) {
                Glide.with(holder.itemView.getContext())
                        .load(lesson.getVideoUrl())
                        .placeholder(R.drawable.placeholder_image) // ảnh tạm khi đang tải
                        .error(R.drawable.error_image)             // ảnh khi tải lỗi
                        .into(page1Holder.thumbnail);
            }
            if (page1Holder.lessonDuration != null) {
                page1Holder.lessonDuration.setText(formatDuration(lesson.getDuration()));
            }
            page1Holder.itemView.setOnClickListener(v -> listener.onLessonClick(lesson));
        } else if (holder instanceof LessonViewHolderPage2) {
            LessonViewHolderPage2 page2Holder = (LessonViewHolderPage2) holder;
            if (page2Holder.lessonNumber != null) {
                page2Holder.lessonNumber.setText(String.format("%02d", lesson.getPosition() != null ? lesson.getPosition() : 0));
            }
            if (page2Holder.lessonTitle != null) {
                page2Holder.lessonTitle.setText(lesson.getTitle() != null ? lesson.getTitle() : "");
            }
            if (page2Holder.lessonDuration != null) {
                page2Holder.lessonDuration.setText(formatDuration(lesson.getDuration()));
            }
            if (page2Holder.lessonIcon != null) {
                page2Holder.lessonIcon.setImageResource(R.drawable.baseline_play_lesson_24);
            }
            page2Holder.itemView.setOnClickListener(v -> listener.onLessonClick(lesson));
        }
    }

    @Override
    public int getItemCount() {
        int size = lessons.size();
        Log.d(TAG, "getItemCount: " + size);
        return size;
    }

    private String formatDuration(Integer seconds) {
        if (seconds == null) return "0 min";
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d mins", minutes, secs);
    }

    public static class LessonViewHolderPage1 extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title, lessonNumber, lessonDuration;

        public LessonViewHolderPage1(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.lesson_thumbnail);
            title = itemView.findViewById(R.id.lesson_title);
            lessonNumber = itemView.findViewById(R.id.lesson_number);
            lessonDuration = itemView.findViewById(R.id.lesson_duration);
        }
    }

    public static class LessonViewHolderPage2 extends RecyclerView.ViewHolder {
        TextView lessonNumber, lessonTitle, lessonDuration;
        ImageView lessonIcon;

        public LessonViewHolderPage2(@NonNull View itemView) {
            super(itemView);
            lessonNumber = itemView.findViewById(R.id.lessonNumber);
            lessonTitle = itemView.findViewById(R.id.lessonTitle);
            lessonDuration = itemView.findViewById(R.id.lessonDuration);
            lessonIcon = itemView.findViewById(R.id.lessonIcon);
        }
    }
}