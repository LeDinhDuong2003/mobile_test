package com.example.mobileproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.CourseDetailActivity;
import com.example.mobileproject.R;
import com.example.mobileproject.adapter.CourseAdapter;
import com.example.mobileproject.api.ApiService;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.CourseList;
import com.example.mobileproject.model.CourseResponse;
import com.example.mobileproject.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteFragment extends Fragment {
    private static final String TAG = "FavoriteFragment";

    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private List<CourseList> courseList = new ArrayList<>();
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());

        // Khởi tạo các thành phần UI
        initUI(view);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Tải danh sách khóa học yêu thích
        loadFavoriteCourses();
    }

    private void initUI(View view) {
        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        progressBar = view.findViewById(R.id.progressBar);

        TextView titleText = view.findViewById(R.id.titleText);
        titleText.setText("Khóa học yêu thích");
    }

    private void setupRecyclerView() {
        // Thiết lập GridLayoutManager với 2 cột
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        // Thiết lập adapter
        courseAdapter = new CourseAdapter(requireContext(), courseList);
        recyclerView.setAdapter(courseAdapter);

        // Thiết lập sự kiện click
        courseAdapter.setOnItemClickListener(course -> {
            // Chuyển đến màn hình chi tiết khóa học
            Intent intent = new Intent(getActivity(), CourseDetailActivity.class);
            intent.putExtra("courseId", Integer.parseInt(course.getId()));
            startActivity(intent);
        });
    }

    private void loadFavoriteCourses() {
        // Hiển thị trạng thái loading
        showLoading();

        // Lấy ID người dùng từ SessionManager
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            showError("Vui lòng đăng nhập để xem danh sách yêu thích");
            return;
        }

        // Gọi API để lấy dữ liệu danh sách yêu thích
        ApiService apiService = RetrofitClient.getClient();
        Call<List<CourseResponse>> call = apiService.getUserWishlists(userId);
        call.enqueue(new Callback<List<CourseResponse>>() {
            @Override
            public void onResponse(Call<List<CourseResponse>> call, Response<List<CourseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CourseResponse> courseResponses = response.body();
                    processFavoriteCourses(courseResponses);
                } else {
                    showError("Không thể tải danh sách yêu thích");
                    Log.e(TAG, "Lỗi API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CourseResponse>> call, Throwable t) {
                showError("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void processFavoriteCourses(List<CourseResponse> courseResponses) {
        // Xóa dữ liệu cũ
        courseList.clear();

        // Chuyển đổi phản hồi thành các đối tượng CourseList
        for (CourseResponse courseResponse : courseResponses) {
            courseList.add(courseResponse.toCourse());
        }

        // Thông báo cho adapter dữ liệu đã thay đổi
        courseAdapter.notifyDataSetChanged();

        // Cập nhật UI dựa trên kết quả
        if (courseList.isEmpty()) {
            showEmpty();
        } else {
            showContent();
        }

        Log.d(TAG, "Đã tải " + courseList.size() + " khóa học yêu thích");
    }

    private void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyTextView != null) emptyTextView.setVisibility(View.GONE);
    }

    private void showContent() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        if (emptyTextView != null) emptyTextView.setVisibility(View.GONE);
    }

    private void showEmpty() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyTextView != null) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText("Bạn chưa có khóa học yêu thích nào");
        }
    }

    private void showError(String message) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }

        if (courseList.isEmpty()) {
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            if (emptyTextView != null) {
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Không thể tải danh sách yêu thích");
            }
        } else {
            // Vẫn hiển thị nội dung nếu có dữ liệu đã cache
            showContent();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại fragment này
        loadFavoriteCourses();
    }
}