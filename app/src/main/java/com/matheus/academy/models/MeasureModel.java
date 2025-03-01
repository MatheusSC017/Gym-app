package com.matheus.academy.models;

public class MeasureModel {
    private Long id;
    private String name;
    private Integer value;
    private Long personalId;

    public MeasureModel(Long id, String name, Integer value, Long personalId) {
        setId(id);
        setName(name);
        setValue(value);
        setPersonalId(personalId);
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

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Long getPersonalId() {
        return personalId;
    }

    public void setPersonalId(Long personalId) {
        this.personalId = personalId;
    }
}
