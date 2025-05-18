package com.example.mobileproject.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.MainActivityHomePage;
import com.example.mobileproject.R;
import com.example.mobileproject.adapter.CourseAdapter;
import com.example.mobileproject.api.RetrofitClient;
import com.example.mobileproject.model.CourseList;
import com.example.mobileproject.model.CourseResponse;
import com.example.mobileproject.repository.DataRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView coursesRecyclerView;
    private CourseAdapter courseAdapter;
    private ImageView bannerImage;
    private TextView topCoursesTitle;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int currentImageIndex = 0;
    private int[] bannerImageResources;
    private List<CourseList> courseList = new ArrayList<>();

    private final Runnable imageRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded() && bannerImage != null) {
                // Thay đổi hình ảnh
                currentImageIndex = (currentImageIndex + 1) % bannerImageResources.length;
                loadBannerImage(bannerImageResources[currentImageIndex]);
                // Lên lịch cho lần tiếp theo
                handler.postDelayed(this, 3000);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy resource IDs cho các hình ảnh banner
        bannerImageResources = DataRepository.getBannerImageResources();

        // Khởi tạo UI
        initUI(view);

        // Thiết lập banner
        setupBanner();

        // Khởi tạo adapter với danh sách trống (sẽ được cập nhật khi có dữ liệu từ API)
        initCourseAdapter();

        // Tải dữ liệu khóa học từ API
        loadCourseDataFromApi();

        TextView seeAllText = view.findViewById(R.id.seeAllText);
        if (seeAllText != null) {
            seeAllText.setOnClickListener(v -> {
                // Mở AllCoursesFragment khi click vào "See All"
                AllCoursesFragment allCoursesFragment = new AllCoursesFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, allCoursesFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void initUI(View view) {
        coursesRecyclerView = view.findViewById(R.id.coursesRecyclerView);
        bannerImage = view.findViewById(R.id.bannerImage);
        topCoursesTitle = view.findViewById(R.id.topCoursesTitle);

        // Thiết lập RecyclerView với GridLayoutManager (2 cột)
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        coursesRecyclerView.setLayoutManager(layoutManager);

        // Thiết lập các sự kiện click
        view.findViewById(R.id.seeAllText).setOnClickListener(v ->
                Toast.makeText(getContext(), "Xem tất cả khóa học", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.searchBarCard).setOnClickListener(v ->
                Toast.makeText(getContext(), "Tìm kiếm khóa học", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBanner() {
        // Hiển thị hình ảnh đầu tiên
        loadBannerImage(bannerImageResources[currentImageIndex]);

        // Bắt đầu thay đổi tự động
        startBannerAutoChange();
    }

    private void loadBannerImage(int imageResource) {
        if (isAdded() && bannerImage != null) {
            // Sử dụng Glide để load resource drawable thay vì URL
//            Glide.with(this)
//                    .load(imageResource)
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(bannerImage);


            // Hoặc đơn giản hơn có thể dùng:
             bannerImage.setImageResource(imageResource);
        }
    }

    private void startBannerAutoChange() {
        handler.postDelayed(imageRunnable, 3000);
    }

    private void initCourseAdapter() {
        // Khởi tạo adapter với danh sách rỗng
        courseAdapter = new CourseAdapter(requireContext(), courseList);

        // Thiết lập sự kiện click cho item
        courseAdapter.setOnItemClickListener(course ->
                Toast.makeText(getContext(),
                        "Khóa học được chọn: " + course.getTitle(),
                        Toast.LENGTH_SHORT).show()
        );

        // Gán adapter cho RecyclerView
        coursesRecyclerView.setAdapter(courseAdapter);
    }

    private void loadCourseDataFromApi() {
        // Hiển thị một loading indicator nếu cần
        // progressBar.setVisibility(View.VISIBLE);

        // Gọi API để lấy top courses
        RetrofitClient.getClient().getTopCourses().enqueue(new Callback<List<CourseResponse>>() {
            @Override
            public void onResponse(Call<List<CourseResponse>> call, Response<List<CourseResponse>> response) {
                // Ẩn loading indicator
                // progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    // Chuyển đổi dữ liệu từ API sang format phù hợp với ứng dụng
                    List<CourseResponse> courseResponses = response.body();
                    List<CourseList> courses = new ArrayList<>();

                    for (CourseResponse courseResponse : courseResponses) {
                        courses.add(courseResponse.toCourse());
                    }

                    // Cập nhật dữ liệu và thông báo cho adapter
                    courseList.clear();
                    courseList.addAll(courses);
                    courseAdapter.notifyDataSetChanged();

                    Log.d(TAG, "Đã tải thành công " + courses.size() + " khóa học");
                } else {
                    // Xử lý khi API trả về lỗi
                    Log.e(TAG, "Lỗi API: " + (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    Toast.makeText(getContext(), "Không thể tải dữ liệu khóa học", Toast.LENGTH_SHORT).show();

                    // Tải dữ liệu mẫu trong trường hợp lỗi
                    loadFallbackData();
                }
            }

            @Override
            public void onFailure(Call<List<CourseResponse>> call, Throwable t) {
                // Ẩn loading indicator
                // progressBar.setVisibility(View.GONE);

                // Xử lý khi có lỗi kết nối
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Tải dữ liệu mẫu trong trường hợp lỗi
                loadFallbackData();
            }
        });
    }

    private void loadFallbackData() {
        // Tải dữ liệu mẫu từ DataRepository trong trường hợp API không hoạt động
        List<CourseList> fallbackCourses = DataRepository.getMockCourses();
        courseList.clear();
        courseList.addAll(fallbackCourses);
        courseAdapter.notifyDataSetChanged();

        Log.d(TAG, "Đã tải " + fallbackCourses.size() + " khóa học từ dữ liệu mẫu");
    }

    @Override
    public void onPause() {
        super.onPause();
        // Dừng thay đổi hình ảnh khi fragment không hiển thị
        handler.removeCallbacks(imageRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tiếp tục thay đổi hình ảnh khi fragment hiển thị lại
        if (bannerImageResources != null && bannerImageResources.length > 0) {
            startBannerAutoChange();
        }
        if (getActivity() instanceof MainActivityHomePage) {
            ((MainActivityHomePage) getActivity()).setMenuButton();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Xóa tất cả callbacks để tránh memory leak
        handler.removeCallbacksAndMessages(null);
    }
}