package com.cavetale.worldmarker;

import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public final class WorldMarkerTest {
    @Test
    public void test() {
        Random random = new Random();
        for (int y = 0; y < 256; y += 2) {
            for (int z = -512; z < 1024; z += random.nextInt(10) + 1) {
                for (int x = -512; x < 1024; x += random.nextInt(10) + 1) {
                    int key = Util.regional(x, y, z);
                    int x2 = Util.regionalX(key);
                    int y2 = Util.regionalY(key);
                    int z2 = Util.regionalZ(key);
                    Assert.assertEquals(x & 511, x2);
                    Assert.assertEquals(y, y2);
                    Assert.assertEquals(z & 511, z2);
                }
            }
        }

        for (int i = 0; i < 1000; i += 1) {
            int x = random.nextInt(10000) - 5000;
            int z = random.nextInt(10000) - 5000;
            long key = Util.toLong(x, z);
            int x2 = Util.xFromLong(key);
            int z2 = Util.zFromLong(key);
            if (x != x2 || z != z2) {
                System.err.println(x + "," + z + " => " + key + " => " + x2 + "," + z2);
            }
            Assert.assertEquals(x, x2);
            Assert.assertEquals(z, z2);
        }
    }
}
