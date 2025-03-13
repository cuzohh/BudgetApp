package com.example.ia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDate;

public class GsonUtil {
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }
}