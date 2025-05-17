package com.example.mobileproject.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.mobileproject.MainActivity;
import com.example.mobileproject.R;
import com.example.mobileproject.adapter.CourseAdapter;
import com.example.mobileproject.model.Course;
import com.example.mobileproject.model.User;
import com.example.mobileproject.repository.DataRepository;

import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView coursesRecyclerView;
    private CourseAdapter courseAdapter;
    private ImageView bannerImage;
    private TextView topCoursesTitle;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int currentImageIndex = 0;
    private String[] bannerImageUrls;

    private final Runnable imageRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded() && bannerImage != null) {
                // Thay đổi hình ảnh
                currentImageIndex = (currentImageIndex + 1) % bannerImageUrls.length;
                loadBannerImage(bannerImageUrls[currentImageIndex]);
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

        // Lấy URL ảnh banner
        bannerImageUrls = DataRepository.getBannerImageUrls();

        // Khởi tạo UI
        initUI(view);

        // Thiết lập banner
        setupBanner();

        // Tải dữ liệu khóa học
        loadCourseData();

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
                Toast.makeText(getContext(), "See all courses clicked", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.searchBarCard).setOnClickListener(v ->
                Toast.makeText(getContext(), "Search clicked", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBanner() {
        // Hiển thị hình ảnh đầu tiên
        loadBannerImage(bannerImageUrls[currentImageIndex]);

        // Bắt đầu thay đổi tự động
        startBannerAutoChange();
    }

    private void loadBannerImage(String imageUrl) {
        if (isAdded() && bannerImage != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(bannerImage);
        }
    }

    private void startBannerAutoChange() {
        handler.postDelayed(imageRunnable, 3000);
    }

    private void loadCourseData() {
        // Lấy dữ liệu khóa học
        List<Course> courseList = DataRepository.getMockCourses();

        // Khởi tạo adapter với dữ liệu
        courseAdapter = new CourseAdapter(requireContext(), courseList);

        // Thiết lập sự kiện click cho item
        courseAdapter.setOnItemClickListener(course ->
                Toast.makeText(getContext(),
                        "Course clicked: " + course.getTitle(),
                        Toast.LENGTH_SHORT).show()
        );

        // Gán adapter cho RecyclerView
        coursesRecyclerView.setAdapter(courseAdapter);
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
        if (bannerImageUrls != null && bannerImageUrls.length > 0) {
            startBannerAutoChange();
        }
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setMenuButton();
        }
    }
}