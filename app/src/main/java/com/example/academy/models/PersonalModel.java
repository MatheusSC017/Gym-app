package com.example.academy.models;

public class PersonalModel {
    private Long id;
    private Float weight;
    private Float height;
    private Float leanMass;
    private Float fatWeight;
    private Float fatPercentage;
    private Float imc;

    public PersonalModel(Long id, Float weight, Float height, Float leanMass, Float fatWeight, Float fatPercentage, Float imc) {
        setId(id);
        setWeight(weight);
        setHeight(height);
        setLeanMass(leanMass);
        setFatWeight(fatWeight);
        setFatPercentage(fatPercentage);
        setImc(imc);
    }

    public Float getFatPercentage() {
        return fatPercentage;
    }

    public void setFatPercentage(Float fatPercentage) {
        this.fatPercentage = fatPercentage;
    }

    public Float getFatWeight() {
        return fatWeight;
    }

    public void setFatWeight(Float fatWeight) {
        this.fatWeight = fatWeight;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getImc() {
        return imc;
    }

    public void setImc(Float imc) {
        this.imc = imc;
    }

    public Float getLeanMass() {
        return leanMass;
    }

    public void setLeanMass(Float leanMass) {
        this.leanMass = leanMass;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }
}
