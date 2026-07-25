package com.merg.diceroller;

import com.merg.diceroller.renderer.texture.DiceTextureGenerator;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class DiceTextureGeneratorTest {
    @Test public void pipCountsMatchFaceValues() {
        for (int value = 1; value <= 6; value++) {
            assertEquals(value, DiceTextureGenerator.pipCenters(value).size());
        }
    }

    @Test public void pipCoordinatesStayInsideTextureBounds() {
        for (int value = 1; value <= 6; value++) {
            List<float[]> centers = DiceTextureGenerator.pipCenters(value);
            for (float[] center : centers) {
                assertTrue(center[0] >= 0f && center[0] <= DiceTextureGenerator.SIZE);
                assertTrue(center[1] >= 0f && center[1] <= DiceTextureGenerator.SIZE);
            }
        }
    }
}
