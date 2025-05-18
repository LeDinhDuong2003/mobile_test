package com.example.mobileproject.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Quiz implements Serializable {
    private Integer quizId;
    private Integer lessonId;
    private String title;
    private LocalDateTime createdAt;
    private Lesson lesson;
    private List<Question> questions = new ArrayList<>();
    private List<QuizResult> quizResults = new ArrayList<>();

    // Constructor
    public Quiz() {
    }

    // Getters and Setters
    public Integer getQuizId() { return quizId; }
    public void setQuizId(Integer quizId) { this.quizId = quizId; }
    public Integer getLessonId() { return lessonId; }
    public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public List<QuizResult> getQuizResults() { return quizResults; }
    public void setQuizResults(List<QuizResult> quizResults) { this.quizResults = quizResults; }
}
