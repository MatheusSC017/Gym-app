package com.example.academy.models;

public class WorkoutModel {
    private Long id;
    private String date;

    public WorkoutModel(Long id, String date) {
        setId(id);
        setDate(date);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
