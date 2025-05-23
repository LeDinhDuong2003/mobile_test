package com.example.mobileproject.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.MainActivityHomePage;
import com.example.mobileproject.R;
import com.example.mobileproject.adapter.InstructorLessonAdapter;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.Lesson;
import com.example.mobileproject.model.LessonCreateRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonManagementFragment extends Fragment implements InstructorLessonAdapter.OnLessonActionListener {

    private static final String TAG = "LessonManagementFragment";
    private static final String ARG_COURSE_ID = "course_id";
    private static final String ARG_COURSE_TITLE = "course_title";

    private RecyclerView lessonsRecyclerView;
    private InstructorLessonAdapter lessonAdapter;
    private FloatingActionButton fabAddLesson;
    private ProgressBar progressBar;
    private TextView tvNoLessons, tvCourseTitle;
    private List<Lesson> courseLessons = new ArrayList<>();
    private int courseId;
    private String courseTitle;

    public static LessonManagementFragment newInstance(int courseId, String courseTitle) {
        LessonManagementFragment fragment = new LessonManagementFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COURSE_ID, courseId);
        args.putString(ARG_COURSE_TITLE, courseTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            courseId = getArguments().getInt(ARG_COURSE_ID);
            courseTitle = getArguments().getString(ARG_COURSE_TITLE);
        }

        // Khi fragment được tạo, đổi nút góc trái thành Back
        if (getActivity() instanceof MainActivityHomePage) {
            ((MainActivityHomePage) getActivity()).setBackButton();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lesson_managerment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupFab();
        loadCourseLessons();
    }

    private void initViews(View view) {
        lessonsRecyclerView = view.findViewById(R.id.lessonsRecyclerView);
        fabAddLesson = view.findViewById(R.id.fabAddLesson);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoLessons = view.findViewById(R.id.tvNoLessons);
        tvCourseTitle = view.findViewById(R.id.tvCourseTitle);

        tvCourseTitle.setText("Quản lý bài học: " + courseTitle);
    }

    private void setupRecyclerView() {
        lessonAdapter = new InstructorLessonAdapter(requireContext(), courseLessons, this);
        lessonsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lessonsRecyclerView.setAdapter(lessonAdapter);
    }

    private void setupFab() {
        fabAddLesson.setOnClickListener(v -> showAddLessonDialog());
    }

    private void loadCourseLessons() {
        showLoading();
        ApiService apiService = RetrofitClient.getClient();
        Call<List<Lesson>> call = apiService.getLessonsByCourseId(courseId);
        call.enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    courseLessons.clear();
                    courseLessons.addAll(response.body());
                    lessonAdapter.notifyDataSetChanged();

                    if (courseLessons.isEmpty()) {
                        showNoLessons();
                    } else {
                        showLessons();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load lessons", Toast.LENGTH_SHORT).show();
                    showNoLessons();
                }
            }

            @Override
            public void onFailure(Call<List<Lesson>> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "Error loading lessons", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showNoLessons();
            }
        });
    }

    private void showAddLessonDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_lesson, null);

        EditText etTitle = dialogView.findViewById(R.id.etLessonTitle);
        EditText etVideoUrl = dialogView.findViewById(R.id.etLessonVideoUrl);
        EditText etDuration = dialogView.findViewById(R.id.etLessonDuration);
        EditText etPosition = dialogView.findViewById(R.id.etLessonPosition);

        // Set default position
        etPosition.setText(String.valueOf(courseLessons.size() + 1));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Thêm bài học mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String videoUrl = etVideoUrl.getText().toString().trim();
                String durationStr = etDuration.getText().toString().trim();
                String positionStr = etPosition.getText().toString().trim();

                if (title.isEmpty()) {
                    etTitle.setError("Vui lòng nhập tiêu đề");
                    return;
                }

                if (videoUrl.isEmpty()) {
                    etVideoUrl.setError("Vui lòng nhập URL video");
                    return;
                }

                int duration = 0;
                if (!durationStr.isEmpty()) {
                    try {
                        duration = Integer.parseInt(durationStr);
                    } catch (NumberFormatException e) {
                        etDuration.setError("Thời lượng không hợp lệ");
                        return;
                    }
                }

                int position = courseLessons.size() + 1;
                if (!positionStr.isEmpty()) {
                    try {
                        position = Integer.parseInt(positionStr);
                    } catch (NumberFormatException e) {
                        etPosition.setError("Vị trí không hợp lệ");
                        return;
                    }
                }

                LessonCreateRequest request = new LessonCreateRequest();
                request.setTitle(title);
                request.setVideoUrl(videoUrl);
                request.setDuration(duration);
                request.setPosition(position);
                request.setCourseId(courseId);

                createLesson(request);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showEditLessonDialog(Lesson lesson) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_lesson, null);

        EditText etTitle = dialogView.findViewById(R.id.etLessonTitle);
        EditText etVideoUrl = dialogView.findViewById(R.id.etLessonVideoUrl);
        EditText etDuration = dialogView.findViewById(R.id.etLessonDuration);
        EditText etPosition = dialogView.findViewById(R.id.etLessonPosition);

        // Điền dữ liệu cũ
        etTitle.setText(lesson.getTitle());
        etVideoUrl.setText(lesson.getVideoUrl());
        etDuration.setText(String.valueOf(lesson.getDuration()));
        etPosition.setText(String.valueOf(lesson.getPosition()));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Sửa bài học")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String videoUrl = etVideoUrl.getText().toString().trim();
                String durationStr = etDuration.getText().toString().trim();
                String positionStr = etPosition.getText().toString().trim();

                if (title.isEmpty()) {
                    etTitle.setError("Vui lòng nhập tiêu đề");
                    return;
                }

                if (videoUrl.isEmpty()) {
                    etVideoUrl.setError("Vui lòng nhập URL video");
                    return;
                }

                int duration = 0;
                if (!durationStr.isEmpty()) {
                    try {
                        duration = Integer.parseInt(durationStr);
                    } catch (NumberFormatException e) {
                        etDuration.setError("Thời lượng không hợp lệ");
                        return;
                    }
                }

                int position = 1;
                if (!positionStr.isEmpty()) {
                    try {
                        position = Integer.parseInt(positionStr);
                    } catch (NumberFormatException e) {
                        etPosition.setError("Vị trí không hợp lệ");
                        return;
                    }
                }

                LessonCreateRequest request = new LessonCreateRequest();
                request.setTitle(title);
                request.setVideoUrl(videoUrl);
                request.setDuration(duration);
                request.setPosition(position);

                updateLesson(lesson.getLessonId(), request);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void createLesson(LessonCreateRequest request) {
        ApiService apiService = RetrofitClient.getClient();
        Call<Lesson> call = apiService.createLesson(request);
        call.enqueue(new Callback<Lesson>() {
            @Override
            public void onResponse(Call<Lesson> call, Response<Lesson> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courseLessons.add(response.body());
                    lessonAdapter.notifyItemInserted(courseLessons.size() - 1);
                    Toast.makeText(getContext(), "Thêm bài học thành công", Toast.LENGTH_SHORT).show();
                    showLessons();
                } else {
                    Toast.makeText(getContext(), "Thêm bài học thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Lesson> call, Throwable t) {
                Log.e(TAG, "Error creating lesson", t);
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLesson(int lessonId, LessonCreateRequest request) {
        ApiService apiService = RetrofitClient.getClient();
        Call<Lesson> call = apiService.updateLesson(lessonId, request);
        call.enqueue(new Callback<Lesson>() {
            @Override
            public void onResponse(Call<Lesson> call, Response<Lesson> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cập nhật trong danh sách
                    for (int i = 0; i < courseLessons.size(); i++) {
                        if (courseLessons.get(i).getLessonId().equals(lessonId)) {
                            courseLessons.set(i, response.body());
                            lessonAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    Toast.makeText(getContext(), "Cập nhật bài học thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Cập nhật bài học thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Lesson> call, Throwable t) {
                Log.e(TAG, "Error updating lesson", t);
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteLesson(int lessonId) {
        ApiService apiService = RetrofitClient.getClient();
        Call<Void> call = apiService.deleteLesson(lessonId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Xóa khỏi danh sách
                    for (int i = 0; i < courseLessons.size(); i++) {
                        if (courseLessons.get(i).getLessonId().equals(lessonId)) {
                            courseLessons.remove(i);
                            lessonAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }

                    if (courseLessons.isEmpty()) {
                        showNoLessons();
                    }

                    Toast.makeText(getContext(), "Xóa bài học thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Xóa bài học thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error deleting lesson", t);
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditLesson(Lesson lesson) {
        showEditLessonDialog(lesson);
    }

    @Override
    public void onDeleteLesson(Lesson lesson) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bài học \"" + lesson.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteLesson(lesson.getLessonId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        lessonsRecyclerView.setVisibility(View.GONE);
        tvNoLessons.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showLessons() {
        lessonsRecyclerView.setVisibility(View.VISIBLE);
        tvNoLessons.setVisibility(View.GONE);
    }

    private void showNoLessons() {
        lessonsRecyclerView.setVisibility(View.GONE);
        tvNoLessons.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Khi fragment bị hủy, đổi lại nút góc trái thành Menu
        if (getActivity() instanceof MainActivityHomePage) {
            ((MainActivityHomePage) getActivity()).setMenuButton();
        }
    }
}