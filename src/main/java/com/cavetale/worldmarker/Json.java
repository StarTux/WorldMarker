package com.cavetale.worldmarker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Json {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final Gson PRETTY = new GsonBuilder()
        .disableHtmlEscaping().setPrettyPrinting().create();

    private Json() { }

    public static String serialize(Object o) {
        return GSON.toJson(o);
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }
}
