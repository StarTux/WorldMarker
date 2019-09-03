package com.cavetale.worldmarker;

import java.util.HashMap;
import java.util.Map;

public final class MarkBlock {
    private String id;
    private Map<String, Object> data;
    transient int x;
    transient int y;
    transient int z;
    transient int key;

    MarkBlock() { }

    void setXYZK(final int nx, final int ny, final int nz, final int nkey) {
        this.x = nx;
        this.y = ny;
        this.z = nz;
        this.key = nkey;
    }

    public boolean hasId() {
        return id != null;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getData() {
        if (data == null) data = new HashMap<>();
        return data;
    }

    public void resetData() {
        data = null;
    }

    public boolean isEmpty() {
        return id == null
            && (data == null || data.isEmpty());
    }
}
