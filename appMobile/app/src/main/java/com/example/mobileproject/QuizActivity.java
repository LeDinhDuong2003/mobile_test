//package com.example.mobileproject;
//
//import android.animation.ValueAnimator;
//import android.content.Intent;
//import android.graphics.Typeface;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//import android.os.Handler;
//import android.text.SpannableString;
//import android.text.style.StyleSpan;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//
//public class QuizActivity extends AppCompatActivity {
//
//    private TextView questionText, resultText, questionNumberText;
//    private Button[] options = new Button[4];
//    private ProgressBar timeProgress;
//    private ValueAnimator progressAnimator;
//    private MediaPlayer correctSound, wrongSound;
//    private int currentQuestion = 0;
//    private int score = 0;
//    private boolean answered = false;
//
//    private String[] questions = {
//            "Hình vuông có cạnh 2 cm có diện tích là:",
//            "Hình vuông có cạnh 3 cm có diện tích là:"
//    };
//
//    private String[][] optionsText = {
//            {"2 cm", "4 dm²", "2 dm²", "4 cm²"},
//            {"9 cm²", "6 dm²", "3 dm²", "9 dm²"}
//    };
//
//    private int[] correctAnswers = {3, 0}; // Index đúng
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_quiz);
//
//        questionNumberText = findViewById(R.id.questionNumber);
//        questionText = findViewById(R.id.questionText);
//        resultText = findViewById(R.id.resultText);
//        timeProgress = findViewById(R.id.timeProgress);
//
//        options[0] = findViewById(R.id.option1);
//        options[1] = findViewById(R.id.option2);
//        options[2] = findViewById(R.id.option3);
//        options[3] = findViewById(R.id.option4);
//
//        correctSound = MediaPlayer.create(this, R.raw.correct);
//        wrongSound = MediaPlayer.create(this, R.raw.wrong);
//
//        for (int i = 0; i < options.length; i++) {
//            int index = i;
//            options[i].setOnClickListener(v -> {
//                if (!answered) {
//                    answered = true;
//                    checkAnswer(index);
//                }
//            });
//        }
//
//        loadQuestion();
//    }
//
//    private void loadQuestion() {
//        if (currentQuestion >= questions.length) return;
//
//        answered = false;
//        questionText.setText(questions[currentQuestion]);
//        resultText.setText("");
//        resultText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
//
//        for (int i = 0; i < 4; i++) {
//            options[i].setText(optionsText[currentQuestion][i]);
//            options[i].setVisibility(View.VISIBLE);
//            options[i].setEnabled(true);
//            options[i].setAlpha(1f);
//            options[i].setBackgroundResource(getOptionBackground(i));
//            options[i].setBackgroundTintList(null); // 💥 Reset lại tint màu để không bị giữ màu cũ
//        }
//        questionNumberText.setText("Câu " + (currentQuestion + 1) + "/" + questions.length);
//        startTimer();
//    }
//
//
//    private void startTimer() {
//        timeProgress.setMax(10000);
//        timeProgress.setProgress(10000);
//
//        if (progressAnimator != null) progressAnimator.cancel();
//
//        progressAnimator = ValueAnimator.ofInt(10000, 0);
//        progressAnimator.setDuration(10000);
//        progressAnimator.addUpdateListener(animation -> {
//            int progress = (int) animation.getAnimatedValue();
//            timeProgress.setProgress(progress);
//        });
//        progressAnimator.addListener(new android.animation.Animator.AnimatorListener() {
//            @Override public void onAnimationStart(android.animation.Animator animation) {}
//            @Override public void onAnimationEnd(android.animation.Animator animation) {
//                if (!answered) {
//                    answered = true;
//                    showCorrectAnswer();
//                }
//            }
//            @Override public void onAnimationCancel(android.animation.Animator animation) {}
//            @Override public void onAnimationRepeat(android.animation.Animator animation) {}
//        });
//        progressAnimator.start();
//    }
//
//    private void checkAnswer(int selectedIndex) {
//        if (progressAnimator != null) progressAnimator.cancel();
//
//        int correctIndex = correctAnswers[currentQuestion];
//
//        for (int i = 0; i < 4; i++) {
//            options[i].setEnabled(false);
//            if (i != selectedIndex && i != correctIndex) {
//                options[i].setAlpha(0.5f); // mờ
//            }
//        }
//
//        if (selectedIndex == correctIndex) {
//            score++;
//            options[correctIndex].setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_light));
//            resultText.setText(boldText("\u2713 Đúng"));
//            resultText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
//            if (correctSound != null) {
//                correctSound.seekTo(0);
//                correctSound.start();
//            }
//        } else {
//            options[selectedIndex].setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_red_light));
//            options[correctIndex].setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_light));
//            resultText.setText(boldText("\u2717 Sai"));
//            resultText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
//            if (wrongSound != null) {
//                wrongSound.seekTo(0);
//                wrongSound.start();
//            }
//        }
//
//        nextAfterDelay();
//    }
//
//    private void showCorrectAnswer() {
//        int correctIndex = correctAnswers[currentQuestion];
//
//        for (int i = 0; i < 4; i++) {
//            options[i].setEnabled(false);
//            if (i != correctIndex) {
//                options[i].setAlpha(0.5f); // làm mờ đáp án sai
//            }
//        }
//
//        options[correctIndex].setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_light));
//        resultText.setText(boldText("\u2717 Sai"));
//        resultText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
//
//        if (wrongSound != null) {
//            wrongSound.seekTo(0);
//            wrongSound.start();
//        }
//
//        nextAfterDelay();
//    }
//
//    private void nextAfterDelay() {
//        new Handler().postDelayed(() -> {
//            if (currentQuestion + 1 >= questions.length) {
//                Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
//                intent.putExtra("score", score);
//                intent.putExtra("total", questions.length);
//                startActivity(intent);
//                finish();
//            } else {
//                currentQuestion++;
//                loadQuestion();
//            }
//        }, 4000);
//    }
//
//    private SpannableString boldText(String text) {
//        SpannableString span = new SpannableString(text);
//        span.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, 0);
//        return span;
//    }
//
//    private int getOptionBackground(int index) {
//        switch (index) {
//            case 0: return R.drawable.option1_background;
//            case 1: return R.drawable.option2_background;
//            case 2: return R.drawable.option3_background;
//            case 3: return R.drawable.option4_background;
//            default: return android.R.color.darker_gray;
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (progressAnimator != null) progressAnimator.cancel();
//        if (correctSound != null) correctSound.release();
//        if (wrongSound != null) wrongSound.release();
//    }
//}

package com.example.mobileproject;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView questionText, resultText, questionNumberText;
    private Button[] options = new Button[4];
    private ProgressBar timeProgress;
    private ValueAnimator progressAnimator;
    private MediaPlayer correctSound, wrongSound;
    private int currentQuestion = 0;
    private int score = 0;
    private boolean answered = false;

    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private static final String TAG = "🔥 quan 🔥";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionNumberText = findViewById(R.id.questionNumber);
        questionText = findViewById(R.id.questionText);
        resultText = findViewById(R.id.resultText);
        timeProgress = findViewById(R.id.timeProgress);

        options[0] = findViewById(R.id.option1);
        options[1] = findViewById(R.id.option2);
        options[2] = findViewById(R.id.option3);
        options[3] = findViewById(R.id.option4);

        correctSound = MediaPlayer.create(this, R.raw.correct);
        wrongSound = MediaPlayer.create(this, R.raw.wrong);

        for (int i = 0; i < options.length; i++) {
            int index = i;
            options[i].setOnClickListener(v -> {
                if (!answered) {
                    answered = true;
                    checkAnswer(index);
                }
            });
        }

        // Lấy dữ liệu từ API
        fetchQuizData();
    }

    private void fetchQuizData() {
        new Thread(() -> {
            try {
                String apiUrl = getString(R.string.base_url) + "/quizzes";
                Log.d(TAG, "🔥 Sending POST to " + apiUrl);
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                // Gửi lesson_id
                JSONObject jsonInput = new JSONObject();
                jsonInput.put("lesson_id", 1);
                String jsonString = jsonInput.toString();
                Log.d(TAG, "🔥 Request body: " + jsonString);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "🔥 Response code: " + responseCode);

                InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                String jsonResponse = response.toString();
                Log.d(TAG, "🔥 Server response: " + jsonResponse);

                runOnUiThread(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        try {
                            JSONArray jsonArray = new JSONArray(jsonResponse);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                QuizQuestion question = new QuizQuestion();
                                question.questionId = json.getInt("question_id");
                                question.content = json.getString("content");
                                JSONArray optionsJson = json.getJSONArray("options");
                                question.options = new String[optionsJson.length()];
                                question.isCorrect = new int[optionsJson.length()];
                                for (int j = 0; j < optionsJson.length(); j++) {
                                    JSONObject option = optionsJson.getJSONObject(j);
                                    question.options[j] = option.getString("content");
                                    question.isCorrect[j] = option.getInt("is_correct");
                                }
                                quizQuestions.add(question);
                            }
                            if (quizQuestions.isEmpty()) {
                                Toast.makeText(this, "Không có câu hỏi nào", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                loadQuestion();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "🔥 JSON parse error: ", e);
                            Toast.makeText(this, "Lỗi phân tích dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Log.e(TAG, "🔥 API error: " + jsonResponse);
                        Toast.makeText(this, "Lỗi tải câu hỏi: " + jsonResponse, Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "🔥 Network error: ", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }

    private void loadQuestion() {
        if (currentQuestion >= quizQuestions.size()) {
            Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
            intent.putExtra("score", score);
            intent.putExtra("total", quizQuestions.size());
            startActivity(intent);
            finish();
            return;
        }

        answered = false;
        QuizQuestion quiz = quizQuestions.get(currentQuestion);
        questionText.setText(quiz.content);
        resultText.setText("");
        resultText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        for (int i = 0; i < 4; i++) {
            options[i].setText(quiz.options[i]);
            options[i].setVisibility(View.VISIBLE);
            options[i].setEnabled(true);
            options[i].setAlpha(1f);
            options[i].setBackgroundResource(getOptionBackground(i));
            options[i].setBackgroundTintList(null);
        }
        questionNumberText.setText("Câu " + (currentQuestion + 1) + "/" + quizQuestions.size());
        startTimer();
    }

    private void startTimer() {
        timeProgress.setMax(10000);
        timeProgress.setProgress(10000);

        if (progressAnimator != null) progressAnimator.cancel();

        progressAnimator = ValueAnimator.ofInt(10000, 0);
        progressAnimator.setDuration(10000);
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            timeProgress.setProgress(progress);
        });
        progressAnimator.addListener(new android.animation.Animator.AnimatorListener() {
            @Override public void onAnimationStart(android.animation.Animator animation) {}
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                if (!answered) {
                    answered = true;
                    showCorrectAnswer();
                }
            }
            @Override public void onAnimationCancel(android.animation.Animator animation) {}
            @Override public void onAnimationRepeat(android.animation.Animator animation) {}
        });
        progressAnimator.start();
    }

    private void checkAnswer(int selectedIndex) {
        if (progressAnimator != null) progressAnimator.cancel();

        QuizQuestion quiz = quizQuestions.get(currentQuestion);
        int correctIndex = -1;
        for (int i = 0; i < quiz.isCorrect.length; i++) {
            if (quiz.isCorrect[i] == 1) {
                correctIndex = i;
                break;
            }
        }

        for (int i = 0; i < 4; i++) {
            options[i].setEnabled(false);
            if (i != selectedIndex && i != correctIndex) {
                options[i].setAlpha(0.5f);
            }
        }

        if (selectedIndex == correctIndex) {
            score++;
            options[correctIndex].setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_light));
            resultText.setText(boldText("\u2713 Đúng"));
            resultText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
            if (correctSound != null) {
                correctSound.seekTo(0);
                correctSound.start();
            }
        } else {
            options[selectedIndex].setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_red_light));
            options[correctIndex].setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_light));
            resultText.setText(boldText("\u2717 Sai"));
            resultText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
            if (wrongSound != null) {
                wrongSound.seekTo(0);
                wrongSound.start();
            }
        }

        nextAfterDelay();
    }

    private void showCorrectAnswer() {
        QuizQuestion quiz = quizQuestions.get(currentQuestion);
        int correctIndex = -1;
        for (int i = 0; i < quiz.isCorrect.length; i++) {
            if (quiz.isCorrect[i] == 1) {
                correctIndex = i;
                break;
            }
        }

        for (int i = 0; i < 4; i++) {
            options[i].setEnabled(false);
            if (i != correctIndex) {
                options[i].setAlpha(0.5f);
            }
        }

        options[correctIndex].setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_light));
        resultText.setText(boldText("\u2717 Sai"));
        resultText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));

        if (wrongSound != null) {
            wrongSound.seekTo(0);
            wrongSound.start();
        }

        nextAfterDelay();
    }

    private void nextAfterDelay() {
        new Handler(getMainLooper()).postDelayed(() -> {
            if (currentQuestion + 1 >= quizQuestions.size()) {
                Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("total", quizQuestions.size());
                if (!quizQuestions.isEmpty()) {
                    intent.putExtra("question_id", quizQuestions.get(0).questionId);
                }
                startActivity(intent);
                finish();
            } else {
                currentQuestion++;
                loadQuestion();
            }
        }, 4000);
    }

    private SpannableString boldText(String text) {
        SpannableString span = new SpannableString(text);
        span.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, 0);
        return span;
    }

    private int getOptionBackground(int index) {
        switch (index) {
            case 0: return R.drawable.option1_background;
            case 1: return R.drawable.option2_background;
            case 2: return R.drawable.option3_background;
            case 3: return R.drawable.option4_background;
            default: return android.R.color.darker_gray;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressAnimator != null) progressAnimator.cancel();
        if (correctSound != null) correctSound.release();
        if (wrongSound != null) wrongSound.release();
    }

    // Class để lưu câu hỏi
    private static class QuizQuestion {
        int questionId;
        String content;
        String[] options;
        int[] isCorrect;
    }
}
