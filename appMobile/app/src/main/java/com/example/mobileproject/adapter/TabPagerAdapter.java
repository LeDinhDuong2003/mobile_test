package com.example.mobileproject.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mobileproject.fragment.LessonsFragment;
import com.example.mobileproject.fragment.OverviewFragment;
import com.example.mobileproject.model.Course;
import com.example.mobileproject.model.Lesson;
import com.example.mobileproject.model.Review;

import java.util.List;

public class TabPagerAdapter extends FragmentStateAdapter {
    private static final String TAG = "TabPagerAdapter";
    private final Course course;
    private final List<Lesson> lessons;
    private final List<Review> reviews;

    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity, Course course, List<Lesson> lessons, List<Review> reviews) {
        super(fragmentActivity);
        this.course = course;
        this.lessons = lessons;
        this.reviews = reviews;
        Log.d(TAG, "TabPagerAdapter created with course: " + (course != null ? course.getTitle() : "null") +
                ", lessons: " + lessons.size() + ", reviews: " + reviews.size());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(TAG, "Creating fragment for position: " + position);
        switch (position) {
            case 0:
                return OverviewFragment.newInstance(course);
            case 1:
                return LessonsFragment.newInstance(course, lessons);
            default:
                return OverviewFragment.newInstance(course);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}