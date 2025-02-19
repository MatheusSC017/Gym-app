package com.example.academy.models;

public class HistoryModel {
    private Long id;
    private String date;
    private Long serieId;

    public HistoryModel(Long id, String date, Long serieId) {
        setId(id);
        setDate(date);
        setSerieId(serieId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getSerieId() {
        return serieId;
    }

    public void setSerieId(Long serieId) {
        this.serieId = serieId;
    }
}
