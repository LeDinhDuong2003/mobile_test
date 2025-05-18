package com.example.mobileproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mobileproject.R;
import com.example.mobileproject.model.CourseList;

import java.util.List;
import java.util.Locale;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.CourseViewHolder> {

    private final Context context;
    private final List<CourseList> courseList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CourseList course);
    }

    public CourseListAdapter(Context context, List<CourseList> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_list, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseList course = courseList.get(position);

        // Load course image từ URL sử dụng Glide
        Glide.with(context)
                .load(course.getImageUrl())
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image))
                .into(holder.courseImage);

        // Hiển thị badge "BEST SELLER" nếu khóa học được đánh dấu
        holder.bestSellerBadge.setVisibility(course.isBestSeller() ? View.VISIBLE : View.GONE);

        // Hiển thị thông tin đánh giá
        holder.ratingBar.setRating(course.getRating());
        holder.ratingText.setText(String.valueOf(course.getRating()));

        // Hiển thị thông tin khóa học
        holder.courseTitle.setText(course.getTitle());
        holder.authorName.setText(course.getAuthorName());
        holder.coursePrice.setText(String.format(Locale.US, "$%.2f", course.getPrice()));

        // Thiết lập sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        ImageView courseImage;
        TextView bestSellerBadge, courseTitle, authorName, coursePrice, ratingText;
        RatingBar ratingBar;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseImage = itemView.findViewById(R.id.courseImage);
            bestSellerBadge = itemView.findViewById(R.id.bestSellerBadge);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ratingText = itemView.findViewById(R.id.ratingText);
            courseTitle = itemView.findViewById(R.id.courseTitle);
            authorName = itemView.findViewById(R.id.authorName);
            coursePrice = itemView.findViewById(R.id.coursePrice);
        }
    }
}