package com.example.ia.model;

import java.io.Serializable;

// Class representing an expense with a category and amount
public class Expense implements Serializable {
    private String category; // The category of the expense
    private double amount; // The amount of the expense


    // Constructor to initialize category and amount
    public Expense(String category, double amount) {
        this.category = category;
        this.amount = amount;
    }
    // Get the category of the expense
    public String getCategory() {
        return category;
    }
    // Get the amount of the expense
    public double getAmount() {
        return amount;
    }
}