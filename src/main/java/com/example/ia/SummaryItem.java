package com.example.ia;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;

public class SummaryItem {
    // Property to hold the category as a string
    private final StringProperty category;
    
    // Property to hold the amount as a double
    private final DoubleProperty amount;

    // Constructor to initialize category and amount
    public SummaryItem(String category, double amount) {
        this.category = new SimpleStringProperty(category);
        this.amount = new SimpleDoubleProperty(amount);
    }

    // Getter for category property
    public StringProperty categoryProperty() {
        return category;
    }

    // Getter for amount property
    public DoubleProperty amountProperty() {
        return amount;
    }
}