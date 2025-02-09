package com.example.academy.models;

public class SerieModel {
    private Long id;
    private String name;
    private Long workoutId;

    public SerieModel(Long id, String name, Long workoutId) {
        setId(id);
        setName(name);
        setWorkoutId(workoutId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(Long workoutId) {
        this.workoutId = workoutId;
    }
}
