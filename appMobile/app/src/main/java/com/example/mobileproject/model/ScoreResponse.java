package com.example.mobileproject.model;

public class ScoreResponse {
    private int course_id;
    private String course_url;
    private String course_title;
    private int lesson_id;
    private String lesson_title;
    private int quiz_id;
    private String quiz_title;
    private String score;
    private String ngaylambai;

    public int getCourse_id() {
        return course_id;
    }

    public String getCourse_url() {
        return course_url;
    }

    public String getCourse_title() {
        return course_title;
    }

    public int getLesson_id() {
        return lesson_id;
    }

    public String getLesson_title() {
        return lesson_title;
    }

    public int getQuiz_id() {
        return quiz_id;
    }

    public String getQuiz_title() {
        return quiz_title;
    }

    public String getScore() {
        return score;
    }

    public String getNgaylambai() {
        return ngaylambai;
    }

    public Score toScore() {
        return new Score(
                course_id,
                course_url,
                course_title,
                lesson_id,
                lesson_title,
                quiz_id,
                quiz_title,
                score,
                ngaylambai
        );
    }
}