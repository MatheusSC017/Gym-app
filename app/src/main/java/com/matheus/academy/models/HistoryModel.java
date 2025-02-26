package com.matheus.academy.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryModel {
    private Long id;
    private Date date;
    private Long serieId;

    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

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

    public Date getDate() {
        return date;
    }

    public String getDateFormatted() {
        return formatter.format(date);
    }

    public void setDate(String date) {
        try {
            this.date = formatter.parse(date);
        } catch (ParseException e) {
            // Do nothing
        }
    }

    public Long getSerieId() {
        return serieId;
    }

    public void setSerieId(Long serieId) {
        this.serieId = serieId;
    }
}
