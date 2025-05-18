package com.example.mobileproject.model;

import java.time.LocalDateTime;

public class QuizResult {
    private Integer quizResultId;
    private Integer userId;
    private Integer quizId;
    private Float totalScore;
    private LocalDateTime completedAt;
    private User user;
    private Quiz quiz;

    // Constructor
    public QuizResult() {
    }

    // Getters and Setters
    public Integer getQuizResultId() { return quizResultId; }
    public void setQuizResultId(Integer quizResultId) { this.quizResultId = quizResultId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getQuizId() { return quizId; }
    public void setQuizId(Integer quizId) { this.quizId = quizId; }
    public Float getTotalScore() { return totalScore; }
    public void setTotalScore(Float totalScore) { this.totalScore = totalScore; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
}
