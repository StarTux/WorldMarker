package com.cavetale.worldmarker;

import org.junit.Assert;
import org.junit.Test;

public final class WorldMarkerTest {
    @Test
    public void test() {
        for (int y = 0; y < 256; y += 1) {
            for (int z = 0; z < 512; z += 1) {
                for (int x = 0; x < 512; x += 1) {
                    int key = Util.regional(x, y, z);
                    int x2 = Util.regionalX(key);
                    int y2 = Util.regionalY(key);
                    int z2 = Util.regionalZ(key);
                    Assert.assertEquals(x, x2);
                    Assert.assertEquals(y, y2);
                    Assert.assertEquals(z, z2);
                }
            }
        }
    }
}
