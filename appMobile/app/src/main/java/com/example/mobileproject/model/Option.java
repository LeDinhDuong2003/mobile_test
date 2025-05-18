package com.example.mobileproject.model;

public class Option {
    private Integer optionId;
    private Integer questionId;
    private String content;
    private Integer isCorrect;
    private Integer position;
    private Question question;

    // Constructor
    public Option() {}

    // Getters and Setters
    public Integer getOptionId() { return optionId; }
    public void setOptionId(Integer optionId) { this.optionId = optionId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Integer isCorrect) { this.isCorrect = isCorrect; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
}
