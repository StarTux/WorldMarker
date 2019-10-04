package com.cavetale.worldmarker;

public final class Util {
    private Util() { }

    static final long LEFT  = 0xFFFFFFFF00000000L;
    static final long RIGHT = 0x00000000FFFFFFFFL;

    static long toLong(final int x, final int z) {
        return (((long) z) << 32) | ((long) x & RIGHT);
    }

    static int xFromLong(final long k) {
        return (int) (k & RIGHT);
    }

    static int zFromLong(final long k) {
        return (int) ((k >> 32) & RIGHT);
    }

    /**
     * Compute the regional key for any given x, y, z coordinates.
     * The stored bits will be the modulo of 512 within [0, 511].
     * MarkRegion uses this to store each block in an appropriate map.
     */
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

    static long now() {
        return System.nanoTime() / 1000000000L;
    }
}
