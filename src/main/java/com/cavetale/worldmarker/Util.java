package com.cavetale.worldmarker;

public final class Util {
    private Util() { }

    static long toLong(final int x, final int z) {
        return ((long) z << 32) | (long) x;
    }

    static int regional(final int x, final int y, final int z) {
        return (z & 511)
            | ((x & 511) << 9)
            | ((y & 255) << 18);
    }

    static int regionalX(final int key) {
        return (key >> 9) & 511;
    }

    static int regionalY(final int key) {
        return key >> 18;
    }

    static int regionalZ(final int key) {
        return key & 511;
    }
}
