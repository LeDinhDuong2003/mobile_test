package com.example.mobileproject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CountdownActivity extends AppCompatActivity {

    private TextView countdownText;
    private MediaPlayer countdownMediaPlayer;
    private MediaPlayer startMediaPlayer;
    private final int[] countdownNumbers = {3, 2, 1};
    private int currentIndex = 0;
    private Handler handler;

    private final int ZOOM_IN_DURATION = 1000;
    private final int DISPLAY_DURATION = 1000;
    private final int ZOOM_OUT_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demnguoc);
        countdownText = findViewById(R.id.countdownText);
        handler = new Handler(Looper.getMainLooper());
        countdownMediaPlayer = MediaPlayer.create(this, R.raw.bell);
        startMediaPlayer = MediaPlayer.create(this, R.raw.start);
        startCountdownAnimation();
    }

    private void startCountdownAnimation() {
        animateNumber(countdownNumbers[currentIndex]);
    }

    private void animateNumber(int number) {
        countdownText.setText(String.valueOf(number));
        countdownText.setScaleX(0.5f);
        countdownText.setScaleY(0.5f);
        countdownText.setAlpha(0.7f);
        playCountdownSound();
        ValueAnimator scaleUpAnimator = ValueAnimator.ofFloat(0.5f, 1.5f);
        scaleUpAnimator.setDuration(ZOOM_IN_DURATION);
        scaleUpAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleUpAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            countdownText.setScaleX(value);
            countdownText.setScaleY(value);
            countdownText.setAlpha(Math.min(1.0f, value));
        });

        ValueAnimator scaleDownAnimator = ValueAnimator.ofFloat(1.5f, 0.5f);
        scaleDownAnimator.setDuration(ZOOM_OUT_DURATION);
        scaleDownAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleDownAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            countdownText.setScaleX(value);
            countdownText.setScaleY(value);
            countdownText.setAlpha(value);
        });

        scaleUpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                handler.postDelayed(scaleDownAnimator::start, DISPLAY_DURATION);
            }
        });

        scaleDownAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentIndex++;
                if (currentIndex < countdownNumbers.length) {
                    animateNumber(countdownNumbers[currentIndex]);
                } else {
                    showStartText();
                }
            }
        });

        scaleUpAnimator.start();
    }

    private void showStartText() {
        countdownText.setText("START");
        countdownText.setScaleX(0.5f);
        countdownText.setScaleY(0.5f);
        countdownText.setAlpha(0.7f);
        playStartSound();
        ValueAnimator scaleUpAnimator = ValueAnimator.ofFloat(0.5f, 1.5f);
        scaleUpAnimator.setDuration(ZOOM_IN_DURATION);
        scaleUpAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleUpAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            countdownText.setScaleX(value);
            countdownText.setScaleY(value);
            countdownText.setAlpha(Math.min(1.0f, value));
        });

        ValueAnimator scaleDownAnimator = ValueAnimator.ofFloat(1.5f, 0.5f);
        scaleDownAnimator.setDuration(ZOOM_OUT_DURATION);
        scaleDownAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleDownAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            countdownText.setScaleX(value);
            countdownText.setScaleY(value);
            countdownText.setAlpha(value);
        });

        scaleUpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                handler.postDelayed(scaleDownAnimator::start, DISPLAY_DURATION);
            }
        });

        scaleDownAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                navigateToNextScreen();
            }
        });

        scaleUpAnimator.start();
    }

    private void playCountdownSound() {
        if (countdownMediaPlayer != null) {
            countdownMediaPlayer.seekTo(0);
            countdownMediaPlayer.start();
        }
    }

    private void playStartSound() {
        if (startMediaPlayer != null) {
            startMediaPlayer.seekTo(0);
            startMediaPlayer.start();
        }
    }

    private void navigateToNextScreen() {
        Intent intent = new Intent(CountdownActivity.this, QuizActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownMediaPlayer != null) {
            countdownMediaPlayer.release();
            countdownMediaPlayer = null;
        }
        if (startMediaPlayer != null) {
            startMediaPlayer.release();
            startMediaPlayer = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}