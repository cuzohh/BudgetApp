package com.example.ia.model;

import java.io.Serializable;

// Represents a source of income.
public class Income implements Serializable {
    // The source of the income (e.g. job, investments, etc.).
    private String source; // source of the income
    // The amount of money made from this source.
    private double amount; // amount of money made from this source

    // Creates a new Income object.
    public Income(String source, double amount) {
        this.source = source;
        this.amount = amount;
    }

    // Gets the source of the income
    public String getSource() {
        return source;
    }

    // Gets the amount of money made from this source.
    public double getAmount() {
        return amount;
    }
}