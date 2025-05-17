package com.example.mobileproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mobileproject.R;
import com.example.mobileproject.model.User;
import com.example.mobileproject.repository.DataRepository;

public class ProfileFragment extends Fragment {

    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy thông tin người dùng
        currentUser = DataRepository.getCurrentUser();

        // Khởi tạo UI
        TextView nameText = view.findViewById(R.id.nameText);
        TextView emailText = view.findViewById(R.id.emailText);
        ImageView profileImage = view.findViewById(R.id.profileImage);

        // Hiển thị thông tin người dùng
        nameText.setText(currentUser.getName());
        emailText.setText(currentUser.getEmail());

        // Tải ảnh người dùng
        Glide.with(this)
                .load(currentUser.getProfileImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .circleCrop()
                .into(profileImage);
    }
}