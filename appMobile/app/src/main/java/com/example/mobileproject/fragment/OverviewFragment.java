package com.example.mobileproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mobileproject.R;
import com.example.mobileproject.model.Course;

public class OverviewFragment extends Fragment {
    private static final String TAG = "OverviewFragment";
    private Course course;

    public static OverviewFragment newInstance(Course course) {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        args.putSerializable("course", course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            course = (Course) getArguments().getSerializable("course");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        TextView introductionText = view.findViewById(R.id.introductionText);
        TextView seeMoreButton = view.findViewById(R.id.seeMoreButton);

        if (introductionText != null) {
            introductionText.setText(course != null && course.getDescription() != null
                    ? course.getDescription() : "No description available");
        }
        if (seeMoreButton != null) {
            seeMoreButton.setOnClickListener(v -> {
                if (introductionText != null) {
                    if (introductionText.getMaxLines() == 3) {
                        introductionText.setMaxLines(Integer.MAX_VALUE);
                        seeMoreButton.setText("See Less");
                    } else {
                        introductionText.setMaxLines(3);
                        seeMoreButton.setText("See More");
                    }
                }
            });
        }

        return view;
    }
}