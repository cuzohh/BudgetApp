package com.example.ia;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;

public class SummaryItem {
    private final StringProperty category;
    private final DoubleProperty amount;

    public SummaryItem(String category, double amount) {
        this.category = new SimpleStringProperty(category);
        this.amount = new SimpleDoubleProperty(amount);
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public DoubleProperty amountProperty() {
        return amount;
    }
}