package com.example.ia.storage;

import com.example.ia.model.Expense;
import com.example.ia.model.Income;
import com.example.ia.model.Subscription;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    private static final String FILE_PATH = "financial_data.dat";

    public static void saveData(List<Income> incomes, List<Expense> expenses, List<Subscription> subscriptions) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(incomes);
            oos.writeObject(expenses);
            oos.writeObject(subscriptions);
        }
    }

    public static List<Object> loadData() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            List<Object> data = new ArrayList<>();
            data.add(ois.readObject());
            data.add(ois.readObject());
            data.add(ois.readObject());
            return data;
        }
    }
}