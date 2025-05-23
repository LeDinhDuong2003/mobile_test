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
import com.example.mobileproject.model.CourseResponse;

import java.util.List;
import java.util.Locale;

public class InstructorCourseAdapter extends RecyclerView.Adapter<InstructorCourseAdapter.CourseViewHolder> {

    private final Context context;
    private final List<CourseResponse> courseList;
    private OnCourseActionListener listener;

    public interface OnCourseActionListener {
        void onEditCourse(CourseResponse course);
        void onDeleteCourse(CourseResponse course);
        void onManageLessons(CourseResponse course);
    }

    public InstructorCourseAdapter(Context context, List<CourseResponse> courseList, OnCourseActionListener listener) {
        this.context = context;
        this.courseList = courseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instructor_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseResponse course = courseList.get(position);

        // Load course image
        Glide.with(context)
                .load(course.getThumbnailUrl())
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image))
                .into(holder.courseImage);

        // Set course info
        holder.courseTitle.setText(course.getTitle());
        holder.courseDescription.setText(course.getDescription());
        holder.coursePrice.setText(String.format(Locale.US, "$%.2f", course.getPrice()));
        holder.courseCategory.setText(course.getCategory());

        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditCourse(course);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteCourse(course);
            }
        });

        holder.btnManageLessons.setOnClickListener(v -> {
            if (listener != null) {
                listener.onManageLessons(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        ImageView courseImage;
        TextView courseTitle, courseDescription, coursePrice, courseCategory;
        ImageView btnEdit, btnDelete, btnManageLessons;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseImage = itemView.findViewById(R.id.courseImage);
            courseTitle = itemView.findViewById(R.id.courseTitle);
            courseDescription = itemView.findViewById(R.id.courseDescription);
            coursePrice = itemView.findViewById(R.id.coursePrice);
            courseCategory = itemView.findViewById(R.id.courseCategory);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnManageLessons = itemView.findViewById(R.id.btnManageLessons);
        }
    }
}