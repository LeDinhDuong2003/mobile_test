package com.example.mobileproject;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mobileproject.adapter.CourseAdapter;
import com.example.mobileproject.model.Course;
import com.example.mobileproject.repository.DataRepository;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView coursesRecyclerView;
    private CourseAdapter courseAdapter;
    private ImageView bannerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initUI();

        // Load banner image
        loadBannerImage();

        // Load course data
        loadCourseData();
    }

    private void initUI() {
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        bannerImage = findViewById(R.id.bannerImage);

        // Set up RecyclerView with a Grid Layout (2 columns)
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        coursesRecyclerView.setLayoutManager(layoutManager);

        // Set other UI event handlers if needed
        findViewById(R.id.seeAllText).setOnClickListener(v ->
                Toast.makeText(this, "See all courses clicked", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.bannerExplore).setOnClickListener(v ->
                Toast.makeText(this, "Banner explore clicked", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadBannerImage() {
        // Load banner image from URL using Glide
        Glide.with(this)
                .load(DataRepository.getBannerImageUrl())
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image))
                .into(bannerImage);
    }

    private void loadCourseData() {
        // Get mock course data
        List<Course> courseList = DataRepository.getMockCourses();

        // Initialize the adapter with course data
        courseAdapter = new CourseAdapter(this, courseList);

        // Set item click listener
        courseAdapter.setOnItemClickListener(course ->
                Toast.makeText(MainActivity.this,
                        "Course clicked: " + course.getTitle(),
                        Toast.LENGTH_SHORT).show()
        );

        // Set the adapter to RecyclerView
        coursesRecyclerView.setAdapter(courseAdapter);
    }
}