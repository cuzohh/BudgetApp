// SummaryItem.java
package com.example.ia;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// Represents an item in the summary table with a category and amount
public class SummaryItem {
    private final StringProperty category; // Category of the summary item
    private final StringProperty amount; // Amount associated with the category

    // Constructor to initialize the category and amount properties
    public SummaryItem(String category, String amount) {
        this.category = new SimpleStringProperty(category);
        this.amount = new SimpleStringProperty(amount);
    }

    // Returns the category property
    public StringProperty categoryProperty() {
        return category;
    }

    // Returns the amount property
    public StringProperty amountProperty() {
        return amount;
    }
}