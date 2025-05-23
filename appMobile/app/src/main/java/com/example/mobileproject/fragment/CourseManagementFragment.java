package com.example.mobileproject.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
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
import com.example.mobileproject.adapter.InstructorCourseAdapter;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.Course;
import com.example.mobileproject.model.CourseCreateRequest;
import com.example.mobileproject.model.CourseResponse;
import com.example.mobileproject.model.PagedResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseManagementFragment extends Fragment implements InstructorCourseAdapter.OnCourseActionListener {

    private static final String TAG = "CourseManagementFragment";
    private RecyclerView coursesRecyclerView;
    private InstructorCourseAdapter courseAdapter;
    private FloatingActionButton fabAddCourse;
    private ProgressBar progressBar;
    private TextView tvNoCourses;
    private List<CourseResponse> instructorCourses = new ArrayList<>();
    private int instructorId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khi fragment được tạo, đổi nút góc trái thành Back
        if (getActivity() instanceof MainActivityHomePage) {
            ((MainActivityHomePage) getActivity()).setBackButton();
        }

        // Lấy instructor ID từ SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("user_info", getContext().MODE_PRIVATE);
        instructorId = prefs.getInt("user_id", -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupFab();
        loadInstructorCourses();
    }

    private void initViews(View view) {
        coursesRecyclerView = view.findViewById(R.id.coursesRecyclerView);
        fabAddCourse = view.findViewById(R.id.fabAddCourse);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoCourses = view.findViewById(R.id.tvNoCourses);
    }

    private void setupRecyclerView() {
        courseAdapter = new InstructorCourseAdapter(requireContext(), instructorCourses, this);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        coursesRecyclerView.setAdapter(courseAdapter);
    }

    private void setupFab() {
        fabAddCourse.setOnClickListener(v -> showAddCourseDialog());
    }

    private void loadInstructorCourses() {
        if (instructorId == -1) {
            Toast.makeText(getContext(), "User data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();
        ApiService apiService = RetrofitClient.getClient();
        Call<List<CourseResponse>> call = apiService.getInstructorCourses(instructorId);
        call.enqueue(new Callback<List<CourseResponse>>() {
            @Override
            public void onResponse(Call<List<CourseResponse>> call, Response<List<CourseResponse>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    instructorCourses.clear();
                    instructorCourses.addAll(response.body());
                    courseAdapter.notifyDataSetChanged();

                    if (instructorCourses.isEmpty()) {
                        showNoCourses();
                    } else {
                        showCourses();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load courses", Toast.LENGTH_SHORT).show();
                    showNoCourses();
                }
            }

            @Override
            public void onFailure(Call<List<CourseResponse>> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "Error loading courses", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showNoCourses();
            }
        });
    }

    private void showAddCourseDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_course, null);

        EditText etTitle = dialogView.findViewById(R.id.etCourseTitle);
        EditText etDescription = dialogView.findViewById(R.id.etCourseDescription);
        EditText etPrice = dialogView.findViewById(R.id.etCoursePrice);
        EditText etCategory = dialogView.findViewById(R.id.etCourseCategory);
        EditText etThumbnailUrl = dialogView.findViewById(R.id.etCourseThumbnailUrl);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Thêm khóa học mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();
                String category = etCategory.getText().toString().trim();
                String thumbnailUrl = etThumbnailUrl.getText().toString().trim();

                if (title.isEmpty()) {
                    etTitle.setError("Vui lòng nhập tiêu đề");
                    return;
                }

                if (description.isEmpty()) {
                    etDescription.setError("Vui lòng nhập mô tả");
                    return;
                }

                double price = 0;
                if (!priceStr.isEmpty()) {
                    try {
                        price = Double.parseDouble(priceStr);
                    } catch (NumberFormatException e) {
                        etPrice.setError("Giá không hợp lệ");
                        return;
                    }
                }

                CourseCreateRequest request = new CourseCreateRequest();
                request.setTitle(title);
                request.setDescription(description);
                request.setPrice(price);
                request.setCategory(category.isEmpty() ? null : category);
                request.setThumbnailUrl(thumbnailUrl.isEmpty() ? null : thumbnailUrl);
                request.setOwnerId(instructorId);

                createCourse(request);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showEditCourseDialog(CourseResponse course) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_course, null);

        EditText etTitle = dialogView.findViewById(R.id.etCourseTitle);
        EditText etDescription = dialogView.findViewById(R.id.etCourseDescription);
        EditText etPrice = dialogView.findViewById(R.id.etCoursePrice);
        EditText etCategory = dialogView.findViewById(R.id.etCourseCategory);
        EditText etThumbnailUrl = dialogView.findViewById(R.id.etCourseThumbnailUrl);

        // Điền dữ liệu cũ
        etTitle.setText(course.getTitle());
        etDescription.setText(course.getDescription());
        etPrice.setText(String.valueOf(course.getPrice()));
        etCategory.setText(course.getCategory());
        etThumbnailUrl.setText(course.getThumbnailUrl());

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Sửa khóa học")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();
                String category = etCategory.getText().toString().trim();
                String thumbnailUrl = etThumbnailUrl.getText().toString().trim();

                if (title.isEmpty()) {
                    etTitle.setError("Vui lòng nhập tiêu đề");
                    return;
                }

                if (description.isEmpty()) {
                    etDescription.setError("Vui lòng nhập mô tả");
                    return;
                }

                double price = 0;
                if (!priceStr.isEmpty()) {
                    try {
                        price = Double.parseDouble(priceStr);
                    } catch (NumberFormatException e) {
                        etPrice.setError("Giá không hợp lệ");
                        return;
                    }
                }

                CourseCreateRequest request = new CourseCreateRequest();
                request.setTitle(title);
                request.setDescription(description);
                request.setPrice(price);
                request.setCategory(category.isEmpty() ? null : category);
                request.setThumbnailUrl(thumbnailUrl.isEmpty() ? null : thumbnailUrl);

                updateCourse(course.getCourseId(), request);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void createCourse(CourseCreateRequest request) {
        ApiService apiService = RetrofitClient.getClient();
        Call<CourseResponse> call = apiService.createCourse(request);
        call.enqueue(new Callback<CourseResponse>() {
            @Override
            public void onResponse(Call<CourseResponse> call, Response<CourseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    instructorCourses.add(response.body());
                    courseAdapter.notifyItemInserted(instructorCourses.size() - 1);
                    Toast.makeText(getContext(), "Thêm khóa học thành công", Toast.LENGTH_SHORT).show();
                    showCourses();
                } else {
                    Toast.makeText(getContext(), "Thêm khóa học thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CourseResponse> call, Throwable t) {
                Log.e(TAG, "Error creating course", t);
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCourse(int courseId, CourseCreateRequest request) {
        ApiService apiService = RetrofitClient.getClient();
        Call<CourseResponse> call = apiService.updateCourse(courseId, request);
        call.enqueue(new Callback<CourseResponse>() {
            @Override
            public void onResponse(Call<CourseResponse> call, Response<CourseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cập nhật trong danh sách
                    for (int i = 0; i < instructorCourses.size(); i++) {
                        if (instructorCourses.get(i).getCourseId() == courseId) {
                            instructorCourses.set(i, response.body());
                            courseAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    Toast.makeText(getContext(), "Cập nhật khóa học thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Cập nhật khóa học thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CourseResponse> call, Throwable t) {
                Log.e(TAG, "Error updating course", t);
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCourse(int courseId) {
        ApiService apiService = RetrofitClient.getClient();
        Call<Void> call = apiService.deleteCourse(courseId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Xóa khỏi danh sách
                    for (int i = 0; i < instructorCourses.size(); i++) {
                        if (instructorCourses.get(i).getCourseId() == courseId) {
                            instructorCourses.remove(i);
                            courseAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }

                    if (instructorCourses.isEmpty()) {
                        showNoCourses();
                    }

                    Toast.makeText(getContext(), "Xóa khóa học thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Xóa khóa học thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error deleting course", t);
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditCourse(CourseResponse course) {
        showEditCourseDialog(course);
    }

    @Override
    public void onDeleteCourse(CourseResponse course) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa khóa học \"" + course.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCourse(course.getCourseId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onManageLessons(CourseResponse course) {
        // Mở fragment quản lý bài học
        LessonManagementFragment fragment = LessonManagementFragment.newInstance(course.getCourseId(), course.getTitle());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        coursesRecyclerView.setVisibility(View.GONE);
        tvNoCourses.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showCourses() {
        coursesRecyclerView.setVisibility(View.VISIBLE);
        tvNoCourses.setVisibility(View.GONE);
    }

    private void showNoCourses() {
        coursesRecyclerView.setVisibility(View.GONE);
        tvNoCourses.setVisibility(View.VISIBLE);
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