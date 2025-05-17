package com.example.mobileproject.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.MainActivity;
import com.example.mobileproject.R;
import com.example.mobileproject.adapter.CategoryAdapter;
import com.example.mobileproject.adapter.CourseListAdapter;
import com.example.mobileproject.model.Category;
import com.example.mobileproject.model.Course;
import com.example.mobileproject.repository.DataRepository;

import java.util.ArrayList;
import java.util.List;

public class AllCoursesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView categoriesRecyclerView;
    private RecyclerView coursesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private CourseListAdapter courseAdapter;
    private TextView tvTitle;
    private ProgressBar loadingProgressBar;
    private List<Course> allCourses;
    private List<Course> displayedCourses = new ArrayList<>();
    private String selectedCategory = null;
    private int currentPage = 0;
    private final int PAGE_SIZE = 5;
    private boolean isLoading = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khi fragment được tạo, đổi nút góc trái thành Back
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBackButton();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_courses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo UI
        initUI(view);

        // Thiết lập RecyclerView cho categories
        setupCategoriesRecyclerView();

        // Thiết lập RecyclerView cho courses
        setupCoursesRecyclerView();

        // Tải dữ liệu khóa học
        loadCourseData();
    }

    private void initUI(View view) {
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        coursesRecyclerView = view.findViewById(R.id.coursesRecyclerView);
        tvTitle = view.findViewById(R.id.tvTitle);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        // Mặc định tiêu đề hiển thị tất cả khóa học
        if (tvTitle != null) {
            tvTitle.setText("Top Courses");
        }

        // Thiết lập sự kiện click
        View searchBarCard = view.findViewById(R.id.searchBarCard);
        if (searchBarCard != null) {
            searchBarCard.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Search clicked", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void setupCategoriesRecyclerView() {
        // Thiết lập RecyclerView cho categories
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        categoriesRecyclerView.setLayoutManager(layoutManager);

        // Lấy danh sách categories
        List<Category> categories = DataRepository.getCategories();

        // Khởi tạo adapter với danh sách categories
        categoryAdapter = new CategoryAdapter(getContext(), categories, this);
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupCoursesRecyclerView() {
        // Thiết lập RecyclerView cho courses
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        coursesRecyclerView.setLayoutManager(layoutManager);

        // Khởi tạo adapter với danh sách courses trống (sẽ được cập nhật sau)
        courseAdapter = new CourseListAdapter(getContext(), displayedCourses);
        courseAdapter.setOnItemClickListener(course ->
                Toast.makeText(getContext(),
                        "Course clicked: " + course.getTitle(),
                        Toast.LENGTH_SHORT).show()
        );
        coursesRecyclerView.setAdapter(courseAdapter);

        // Thêm scroll listener để load more
        coursesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (linearLayoutManager != null) {
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

                    if (!isLoading) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= PAGE_SIZE) {
                            loadMoreCourses();
                        }
                    }
                }
            }
        });
    }

    private void loadCourseData() {
        // Lấy tất cả khóa học
        allCourses = DataRepository.getAllCourses();

        // Reset các biến
        currentPage = 0;
        displayedCourses.clear();

        // Tải trang đầu tiên
        loadMoreCourses();
    }

    private void showLoadingView() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingView() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    private void loadMoreCourses() {
        isLoading = true;

        // Hiển thị loading indicator
        showLoadingView();

        // Giả lập thời gian tải để thấy hiệu ứng (có thể bỏ trong thực tế)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Lọc khóa học theo category nếu có
            List<Course> filteredCourses = filterCoursesByCategory();

            // Tính toán số lượng item cần lấy
            int startPosition = currentPage * PAGE_SIZE;
            if (startPosition >= filteredCourses.size()) {
                // Đã hết dữ liệu
                isLoading = false;
                hideLoadingView();
                return;
            }

            int endPosition = Math.min(startPosition + PAGE_SIZE, filteredCourses.size());
            List<Course> newCourses = filteredCourses.subList(startPosition, endPosition);

            // Thêm khóa học mới vào danh sách hiển thị
            int prevSize = displayedCourses.size();
            displayedCourses.addAll(newCourses);
            courseAdapter.notifyItemRangeInserted(prevSize, newCourses.size());

            // Tăng page hiện tại
            currentPage++;
            isLoading = false;
            hideLoadingView();
        }, 1000); // Giả lập thời gian tải 1 giây
    }

    private List<Course> filterCoursesByCategory() {
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            return allCourses;
        }

        List<Course> filteredCourses = new ArrayList<>();
        for (Course course : allCourses) {
            if (course.getCategory() != null && course.getCategory().equals(selectedCategory)) {
                filteredCourses.add(course);
            }
        }
        return filteredCourses;
    }

    @Override
    public void onCategoryClick(Category category) {
        // Cập nhật category đang chọn
        selectedCategory = category.getName();

        // Cập nhật tiêu đề
        tvTitle.setText("Top Courses in " + selectedCategory);

        // Reset các biến
        currentPage = 0;
        displayedCourses.clear();
        courseAdapter.notifyDataSetChanged();

        // Tải lại dữ liệu khóa học
        loadMoreCourses();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Khi fragment bị hủy, đổi lại nút góc trái thành Menu
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setMenuButton();
        }
    }
}