package com.example.academy.models;

public class ExerciseModel {
    private Long id;
    private String name;
    private Integer seriesNumber;
    private String measure;
    private Integer quantity;
    private String muscle;
    private Integer sequence = 0;
    private String observation;
    private Long serieId;

    public ExerciseModel(Long id, String name, Integer series_number, String measure, Integer quantity,
                         String muscle, Integer sequence, String observation, Long serieId) {
        setId(id);
        setName(name);
        setSeriesNumber(series_number);
        setMeasure(measure);
        setQuantity(quantity);
        setMuscle(muscle);
        setSequence(sequence);
        setObservation(observation);
        setSerieId(serieId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getMuscle() {
        return muscle;
    }

    public void setMuscle(String muscle) {
        this.muscle = muscle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Long getSerieId() {
        return serieId;
    }

    public void setSerieId(Long serieId) {
        this.serieId = serieId;
    }

    public Integer getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(Integer series_number) {
        this.seriesNumber = series_number;
    }
}
