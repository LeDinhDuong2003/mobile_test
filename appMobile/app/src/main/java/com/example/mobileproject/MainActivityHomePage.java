package com.example.mobileproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.mobileproject.fragment.FavoriteFragment;
import com.example.mobileproject.fragment.HomeFragment;
import com.example.mobileproject.fragment.CoursesFragment;
import com.example.mobileproject.fragment.ProfileFragment;
import com.example.mobileproject.model.UserMain;
import com.example.mobileproject.repository.DataRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivityHomePage extends AppCompatActivity {

    private TextView tvGreeting;
    private CardView btnLeftAction;
    private ImageView leftActionIcon;
    private boolean isBackButton = false;
    private UserMain currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lấy thông tin người dùng
        currentUser = DataRepository.getCurrentUser();

        // Khởi tạo UI
        initUI();

        // Hiển thị fragment Home mặc định
        loadFragment(new HomeFragment());
    }

    private void initUI() {
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLeftAction = findViewById(R.id.btnLeftAction);
        leftActionIcon = findViewById(R.id.leftActionIcon);

        if (tvGreeting != null) {
            tvGreeting.setText(currentUser.getGreeting());
        }

        // Thiết lập Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView != null) {
            // Đảm bảo chọn item Home mặc định
            bottomNavigationView.setSelectedItemId(R.id.nav_home);

            // Thiết lập listener
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    setMenuButton(); // Chuyển về Menu khi về Home
                }
                else if (itemId == R.id.nav_favorite) {
                    selectedFragment = new FavoriteFragment();
                    setMenuButton(); // Reset về Menu cho các tabs khác
                }
                else if (itemId == R.id.nav_courses) {
                    selectedFragment = new CoursesFragment();
                    setMenuButton(); // Reset về Menu cho các tabs khác
                }
                else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    setMenuButton(); // Reset về Menu cho các tabs khác
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }

                return false;
            });
        }

        // Thiết lập sự kiện cho nút ở góc trái (Menu hoặc Back)
        if (btnLeftAction != null) {
            btnLeftAction.setOnClickListener(v -> {
                if (isBackButton) {
                    // Nếu là nút Back, quay lại fragment trước đó
                    onBackPressed();
                } else {
                    // Nếu là nút Menu, mở menu
                    Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Thiết lập sự kiện cho nút Cart
        View btnCart = findViewById(R.id.btnCart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                // Xử lý khi nhấn nút Cart
                Toast.makeText(this, "Cart clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Phương thức để chuyển sang nút Menu
    public void setMenuButton() {
        if (leftActionIcon != null) {
            leftActionIcon.setImageResource(R.drawable.ic_menu);
            isBackButton = false;
        }
    }

    // Phương thức để chuyển sang nút Back
    public void setBackButton() {
        if (leftActionIcon != null) {
            leftActionIcon.setImageResource(R.drawable.ic_back);
            isBackButton = true;
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}