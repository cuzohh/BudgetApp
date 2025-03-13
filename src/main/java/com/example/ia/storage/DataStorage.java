package com.example.ia.storage;

import com.example.ia.model.Expense;
import com.example.ia.model.Income;
import com.example.ia.model.Subscription;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    private static final String FILE_PATH = "financial_data.dat"; // Path to the data file

    // Save data to file
    public static void saveData(List<Income> incomes, List<Expense> expenses, List<Subscription> subscriptions) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(incomes); // Write incomes to file
            oos.writeObject(expenses); // Write expenses to file
            oos.writeObject(subscriptions); // Write subscriptions to file
        }
    }

    // Load data from file
    public static List<Object> loadData() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            List<Object> data = new ArrayList<>(); // List to store data
            data.add(ois.readObject()); // Read incomes from file
            data.add(ois.readObject()); // Read expenses from file
            data.add(ois.readObject()); // Read subscriptions from file
            return data; // Return loaded data
        }
    }
}