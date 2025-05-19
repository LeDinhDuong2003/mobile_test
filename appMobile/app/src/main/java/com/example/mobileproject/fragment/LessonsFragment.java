package com.example.mobileproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.VideoPlayerActivity;
import com.example.mobileproject.adapter.LessonAdapter;
import com.example.mobileproject.model.Course;
import com.example.mobileproject.model.Lesson;

import java.util.ArrayList;
import java.util.List;

public class LessonsFragment extends Fragment {
    private static final String TAG = "LessonsFragment";
    private Course course;
    private List<Lesson> lessons;

    public static LessonsFragment newInstance(Course course, List<Lesson> lessons) {
        LessonsFragment fragment = new LessonsFragment();
        Bundle args = new Bundle();
        args.putSerializable("course", course);
        args.putSerializable("lessons", new ArrayList<>(lessons)); // Ensure serializable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            course = (Course) getArguments().getSerializable("course");
            lessons = (List<Lesson>) getArguments().getSerializable("lessons");
            Log.d(TAG, "Course: " + (course != null ? course.getTitle() : "null") +
                    ", Lessons size: " + (lessons != null ? lessons.size() : "null"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lessons, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.lessonsRecyclerView);
        if (recyclerView == null) {
            Log.e(TAG, "lessonsRecyclerView not found");
            Toast.makeText(getContext(), "Error: RecyclerView not found", Toast.LENGTH_LONG).show();
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (lessons == null || lessons.isEmpty()) {
            Log.w(TAG, "Lessons is null or empty. Creating mock lessons");
            // Tạo dữ liệu mẫu
            lessons = createMockLessons();
        }

        LessonAdapter adapter = new LessonAdapter(lessons, LessonAdapter.TYPE_PAGE_1, lesson -> {
            if (getActivity() == null) {
                Log.e(TAG, "getActivity() is null");
                Toast.makeText(getContext(), "Error: Activity not available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra dữ liệu bài học
            if (lesson == null) {
                Log.e(TAG, "Lesson is null");
                Toast.makeText(getContext(), "Error: Invalid lesson data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đảm bảo lessonId không null
            int lessonId = lesson.getLessonId() != null ? lesson.getLessonId() : -1;

            // Đảm bảo courseId
            int courseId = course != null && course.getCourseId() != null ? course.getCourseId() : -1;

            Log.d(TAG, "Mở VideoPlayerActivity với lessonId=" + lessonId + ", courseId=" + courseId);

            Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
            intent.putExtra("lessonId", lessonId);
            intent.putExtra("courseId", courseId);

            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting VideoPlayerActivity", e);
                Toast.makeText(getContext(), "Lỗi mở video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "LessonsRecyclerView set with " + lessons.size() + " lessons");
        return view;
    }

    // Tạo dữ liệu mẫu khi không có dữ liệu từ API
    private List<Lesson> createMockLessons() {
        List<Lesson> mockLessons = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Lesson lesson = new Lesson();
            lesson.setLessonId(i);
            lesson.setCourseId(course != null ? course.getCourseId() : 1);
            lesson.setTitle("Bài học mẫu " + i);
            lesson.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
            lesson.setPosition(i);
            lesson.setDuration(i * 300); // 5-25 phút
            mockLessons.add(lesson);
        }
        return mockLessons;
    }
}