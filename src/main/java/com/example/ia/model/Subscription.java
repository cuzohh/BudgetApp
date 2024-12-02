package com.example.ia.model;

import java.io.Serializable;

public class Subscription implements Serializable {
    private String name;
    private double cost;

    public Subscription(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}