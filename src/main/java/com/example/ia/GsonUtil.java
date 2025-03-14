package com.example.ia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDate;

public class GsonUtil {
    // Creates and returns a Gson instance with custom type adapters
    public static Gson createGson() {
        return new GsonBuilder()
                // Register type adapter for LocalDate to handle serialization and deserialization
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }
}