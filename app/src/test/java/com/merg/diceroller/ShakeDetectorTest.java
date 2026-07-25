package com.merg.diceroller;

import com.merg.diceroller.sensor.ShakeDetector;
import org.junit.Test;
import static org.junit.Assert.*;

public class ShakeDetectorTest {
    @Test public void cooldownBlocksRapidShake() {
        assertFalse(ShakeDetector.cooldownAllows(10_000L, 10_500L));
        assertTrue(ShakeDetector.cooldownAllows(10_000L, 11_500L));
    }
}
