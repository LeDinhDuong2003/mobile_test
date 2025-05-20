package com.example.mobileproject.model;

public class Score {
    private int courseId;
    private String courseUrl;
    private String courseTitle;
    private int lessonId;
    private String lessonTitle;
    private int quizId;
    private String quizTitle;
    private String score;
    private String ngaylambai;

    public Score(int courseId, String courseUrl, String courseTitle, int lessonId, String lessonTitle,
                 int quizId, String quizTitle, String score, String ngaylambai) {
        this.courseId = courseId;
        this.courseUrl = courseUrl;
        this.courseTitle = courseTitle;
        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.score = score;
        this.ngaylambai = ngaylambai;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseUrl() {
        return courseUrl;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public int getLessonId() {
        return lessonId;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public int getQuizId() {
        return quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public String getScore() {
        return score;
    }

    public String getNgaylambai() {
        return ngaylambai;
    }
}