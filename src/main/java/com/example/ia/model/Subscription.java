package com.example.ia.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Subscription implements Serializable {
    private String name;
    private double cost;
    private LocalDate startDate;
    private String recurrencePeriod;
    private int notificationDays;

    public Subscription(String name, double cost, LocalDate startDate, String recurrencePeriod, int notificationDays) {
        this.name = name;
        this.cost = cost;
        this.startDate = startDate;
        this.recurrencePeriod = recurrencePeriod;
        this.notificationDays = notificationDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getRecurrencePeriod() {
        return recurrencePeriod;
    }

    public void setRecurrencePeriod(String recurrencePeriod) {
        this.recurrencePeriod = recurrencePeriod;
    }

    public int getNotificationDays() {
        return notificationDays;
    }

    public void setNotificationDays(int notificationDays) {
        this.notificationDays = notificationDays;
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