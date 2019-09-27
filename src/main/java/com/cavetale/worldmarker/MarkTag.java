package com.cavetale.worldmarker;

import java.util.HashMap;
import java.util.Map;

final class MarkTag {
    String id;
    private Map<String, Object> data;

    Map<String, Object> getData() {
        if (data == null) data = new HashMap<>();
        return data;
    }

    public boolean isEmpty() {
        return id == null && (data == null || data.isEmpty());
    }
}
