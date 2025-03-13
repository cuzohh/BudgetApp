package com.example.ia.model;

import java.io.Serializable;

/**
 * Represents a source of income.
 */
public class Income implements Serializable {
    // The source of the income (e.g. job, investments, etc.).
    private String source;
    // The amount of money made from this source.
    private double amount;

    /**
     * Creates a new Income object.
     * @param source The source of the income.
     * @param amount The amount of money made from this source.
     */
    public Income(String source, double amount) {
        this.source = source;
        this.amount = amount;
    }

    /**
     * Gets the source of the income.
     * @return The source of the income.
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source of the income.
     * @param source The new source of the income.
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Gets the amount of money made from this source.
     * @return The amount of money made from this source.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the amount of money made from this source.
     * @param amount The new amount of money made from this source.
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }
}