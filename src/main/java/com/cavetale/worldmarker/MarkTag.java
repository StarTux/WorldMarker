package com.cavetale.worldmarker;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom data storage used for blocks, chunks, worlds.
 *
 * This class should not be exposed to the client plugin.  Its
 * properties and method should be encapsulated by public methods of
 * the containing class.
 */
final class MarkTag {
    String id;
    private Map<String, Object> data;

    boolean hasData() {
        return data != null && !data.isEmpty();
    }

    Map<String, Object> getData() {
        if (data == null) data = new HashMap<>();
        return data;
    }

    void resetData() {
        data = null;
    }

    public boolean isEmpty() {
        return id == null && (data == null || data.isEmpty());
    }

    @Override
    public String toString() {
        return "id=" + id
            + " data=" + Json.serialize(data);
    }
}
