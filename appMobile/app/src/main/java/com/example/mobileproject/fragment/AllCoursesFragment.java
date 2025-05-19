package com.example.mobileproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.CourseDetailActivity;
import com.example.mobileproject.MainActivityHomePage;
import com.example.mobileproject.R;
import com.example.mobileproject.adapter.CategoryAdapter;
import com.example.mobileproject.adapter.CourseListAdapter;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.Category;
import com.example.mobileproject.model.CourseList;
import com.example.mobileproject.model.CourseResponse;
import com.example.mobileproject.model.PagedResponse;
import com.example.mobileproject.repository.DataRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllCoursesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "AllCoursesFragment";
    private static final int PAGE_SIZE = 5;
    private static final long LOAD_MORE_DELAY = 1000; // 1 giây delay

    private RecyclerView categoriesRecyclerView;
    private RecyclerView coursesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private CourseListAdapter courseAdapter;
    private TextView tvTitle;
    private TextView tvNoCourses;
    private FrameLayout initialLoadingLayout;
    private FrameLayout loadMoreLayout;
    private EditText searchEditText;
    private CardView btnSearch;

    private List<CourseList> displayedCourses = new ArrayList<>();
    private String selectedCategory = null;
    private String searchQuery = null;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khi fragment được tạo, đổi nút góc trái thành Back
        if (getActivity() instanceof MainActivityHomePage) {
            ((MainActivityHomePage) getActivity()).setBackButton();
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

        // Thiết lập tìm kiếm
        setupSearch(view);

        // Tải dữ liệu khóa học ban đầu
        resetAndLoadCourses();
    }

    private void initUI(View view) {
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        coursesRecyclerView = view.findViewById(R.id.coursesRecyclerView);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvNoCourses = view.findViewById(R.id.tvNoCourses);
        initialLoadingLayout = view.findViewById(R.id.initialLoadingLayout);
        loadMoreLayout = view.findViewById(R.id.loadMoreLayout);
        searchEditText = view.findViewById(R.id.searchEditText);
        btnSearch = view.findViewById(R.id.btnSearch);

        // Mặc định tiêu đề hiển thị tất cả khóa học
        if (tvTitle != null) {
            tvTitle.setText("Tất cả khóa học");
        }
    }

    private void setupSearch(View view) {
        // Thiết lập sự kiện tìm kiếm khi nhấn nút Search
        btnSearch.setOnClickListener(v -> {
            searchQuery = searchEditText.getText().toString().trim();
            if (searchQuery.isEmpty()) {
                searchQuery = null;
            }
            resetAndLoadCourses();
        });

        // Thiết lập sự kiện tìm kiếm khi nhấn Enter trên bàn phím
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                searchQuery = searchEditText.getText().toString().trim();
                if (searchQuery.isEmpty()) {
                    searchQuery = null;
                }
                resetAndLoadCourses();
                return true;
            }
            return false;
        });
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
        courseAdapter.setOnItemClickListener(course -> {
            // Tạo intent để mở CourseDetailActivity
            Intent intent = new Intent(getActivity(), CourseDetailActivity.class);
            // Truyền course_id vào intent
            intent.putExtra("courseId", Integer.parseInt(course.getId()));
            // Khởi chạy activity
            startActivity(intent);
        });
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

                    if (!isLoading && hasMorePages) {
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

    private void resetAndLoadCourses() {
        // Reset các biến
        currentPage = 0;
        displayedCourses.clear();
        courseAdapter.notifyDataSetChanged();
        hasMorePages = true;

        // Ẩn thông báo không có khóa học
        tvNoCourses.setVisibility(View.GONE);

        // Cập nhật tiêu đề
        updateTitle();

        // Hiển thị loading ban đầu
        showInitialLoadingView();

        // Tải trang đầu tiên
        fetchCourses();
    }

    private void updateTitle() {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            tvTitle.setText("Kết quả tìm kiếm: " + searchQuery);
        } else if (selectedCategory != null) {
            tvTitle.setText("Khóa học " + selectedCategory);
        } else {
            tvTitle.setText("Tất cả khóa học");
        }
    }

    private void showInitialLoadingView() {
        initialLoadingLayout.setVisibility(View.VISIBLE);
        coursesRecyclerView.setVisibility(View.GONE);
        loadMoreLayout.setVisibility(View.GONE);
    }

    private void hideInitialLoadingView() {
        initialLoadingLayout.setVisibility(View.GONE);
        coursesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoadMoreView() {
        loadMoreLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoadMoreView() {
        loadMoreLayout.setVisibility(View.GONE);
    }

    private void loadMoreCourses() {
        if (isLoading || !hasMorePages) return;

        isLoading = true;
        showLoadMoreView();

        // Delay 1 giây trước khi tải thêm dữ liệu
        handler.postDelayed(this::fetchCourses, LOAD_MORE_DELAY);
    }

    private void fetchCourses() {
        // Sử dụng API thống nhất với các tham số tùy chọn
        RetrofitClient.getClient().getCourses(currentPage, PAGE_SIZE, selectedCategory, searchQuery)
                .enqueue(new Callback<PagedResponse<CourseResponse>>() {
                    @Override
                    public void onResponse(Call<PagedResponse<CourseResponse>> call, Response<PagedResponse<CourseResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PagedResponse<CourseResponse> pagedResponse = response.body();

                            // Chuyển đổi từ CourseResponse sang Course
                            List<CourseList> newCourses = new ArrayList<>();
                            for (CourseResponse courseResponse : pagedResponse.getItems()) {
                                newCourses.add(courseResponse.toCourse());
                            }

                            // Cập nhật dữ liệu và adapter
                            int insertPosition = displayedCourses.size();
                            displayedCourses.addAll(newCourses);
                            courseAdapter.notifyItemRangeInserted(insertPosition, newCourses.size());

                            // Cập nhật trạng thái phân trang
                            currentPage++;
                            hasMorePages = pagedResponse.hasMorePages();

                            // Hiển thị thông báo khi không có khóa học
                            if (displayedCourses.isEmpty()) {
                                tvNoCourses.setVisibility(View.VISIBLE);
                                coursesRecyclerView.setVisibility(View.GONE);
                            } else {
                                tvNoCourses.setVisibility(View.GONE);
                                coursesRecyclerView.setVisibility(View.VISIBLE);
                            }

                            Log.d(TAG, "Đã tải " + newCourses.size() + " khóa học, trang " + pagedResponse.getPage() +
                                    ", còn trang tiếp theo: " + hasMorePages);
                        } else {
                            // Xử lý lỗi API
                            Log.e(TAG, "Lỗi API: " + response.code());
                            Toast.makeText(getContext(), "Không thể tải dữ liệu khóa học", Toast.LENGTH_SHORT).show();

                            // Nếu là lần đầu tải và không có dữ liệu, load dữ liệu mẫu
                            if (displayedCourses.isEmpty()) {
                                loadFallbackData();
                            }
                        }

                        isLoading = false;
                        hideInitialLoadingView();
                        hideLoadMoreView();
                    }

                    @Override
                    public void onFailure(Call<PagedResponse<CourseResponse>> call, Throwable t) {
                        Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                        // Nếu là lần đầu tải và không có dữ liệu, load dữ liệu mẫu
                        if (displayedCourses.isEmpty()) {
                            loadFallbackData();
                        }

                        isLoading = false;
                        hideInitialLoadingView();
                        hideLoadMoreView();
                    }
                });
    }

    private void loadFallbackData() {
        // Tải dữ liệu mẫu từ DataRepository trong trường hợp API không hoạt động
        List<CourseList> fallbackCourses;
        if (selectedCategory != null) {
            fallbackCourses = DataRepository.getCoursesByCategory(selectedCategory);
        } else if (searchQuery != null && !searchQuery.isEmpty()) {
            // Giả lập tìm kiếm trong dữ liệu mẫu
            fallbackCourses = new ArrayList<>();
            for (CourseList course : DataRepository.getAllCourses()) {
                if (course.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) {
                    fallbackCourses.add(course);
                }
            }
        } else {
            fallbackCourses = DataRepository.getAllCourses();
            // Giới hạn số lượng để giả lập phân trang
            if (fallbackCourses.size() > 5) {
                fallbackCourses = fallbackCourses.subList(0, 5);
            }
        }

        displayedCourses.clear();
        displayedCourses.addAll(fallbackCourses);
        courseAdapter.notifyDataSetChanged();

        // Hiển thị thông báo khi không có khóa học
        if (displayedCourses.isEmpty()) {
            tvNoCourses.setVisibility(View.VISIBLE);
            coursesRecyclerView.setVisibility(View.GONE);
        } else {
            tvNoCourses.setVisibility(View.GONE);
            coursesRecyclerView.setVisibility(View.VISIBLE);
        }

        // Để có thể load more với dữ liệu giả lập
        currentPage = 1;
        hasMorePages = fallbackCourses.size() >= PAGE_SIZE;

        Log.d(TAG, "Đã tải " + fallbackCourses.size() + " khóa học từ dữ liệu mẫu");
    }

    @Override
    public void onCategoryClick(Category category) {
        // Xóa tìm kiếm hiện tại nếu có
        if (searchEditText != null) {
            searchEditText.setText("");
        }
        searchQuery = null;

        // Cập nhật category đang chọn
        selectedCategory = category.getName();

        // Reset và tải lại dữ liệu
        resetAndLoadCourses();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Xóa tất cả các callback đang chờ
        handler.removeCallbacksAndMessages(null);

        // Khi fragment bị hủy, đổi lại nút góc trái thành Menu
        if (getActivity() instanceof MainActivityHomePage) {
            ((MainActivityHomePage) getActivity()).setMenuButton();
        }
    }
}