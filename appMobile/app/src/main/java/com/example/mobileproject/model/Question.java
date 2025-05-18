package com.example.mobileproject.model;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private Integer questionId;
    private Integer quizId;
    private String content;
    private String questionType;
    private Quiz quiz;
    private List<Option> options = new ArrayList<>();

    // Constructor
    public Question() {}

    // Getters and Setters
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public Integer getQuizId() { return quizId; }
    public void setQuizId(Integer quizId) { this.quizId = quizId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }
}
