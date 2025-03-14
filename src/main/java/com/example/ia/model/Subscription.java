package com.example.ia.model;

import java.io.Serializable;
import java.time.LocalDate;

// Represents a recurring subscription (e.g. monthly, yearly).
public class Subscription implements Serializable {
    private String name; // name of the subscription
    private double cost; // cost of the subscription
    private LocalDate startDate; // start date of the subscription
    private String recurrencePeriod; // how often the subscription recurs (e.g. weekly, monthly, yearly)
    private int notificationDays; // number of days before the subscription is due

    // Constructor
    public Subscription(String name, double cost, LocalDate startDate, String recurrencePeriod, int notificationDays) {
        this.name = name;
        this.cost = cost;
        this.startDate = startDate;
        this.recurrencePeriod = recurrencePeriod;
        this.notificationDays = notificationDays;
    }

    // Gets the start date of the subscription
    public LocalDate getStartDate() {
        return startDate;
    }

    // Sets the start date of the subscription
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    // Gets the recurrence period of the subscription
    public String getRecurrencePeriod() {
        return recurrencePeriod;
    }

    // Sets the recurrence period of the subscription
    public void setRecurrencePeriod(String recurrencePeriod) {
        this.recurrencePeriod = recurrencePeriod;
    }

    // Gets the number of days before the subscription is due
    public int getNotificationDays() {
        return notificationDays;
    }

    // Sets the number of days before the subscription is due
    public void setNotificationDays(int notificationDays) {
        this.notificationDays = notificationDays;
    }

    // Gets the name of the subscription
    public String getName() {
        return name;
    }

    // Sets the name of the subscription
    public void setName(String name) {
        this.name = name;
    }

    // Gets the cost of the subscription
    public double getCost() {
        return cost;
    }

    // Sets the cost of the subscription
    public void setCost(double cost) {
        this.cost = cost;
    }
}