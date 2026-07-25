package com.merg.diceroller;

import com.merg.diceroller.renderer.DiceFaceMapping;
import com.merg.diceroller.renderer.DiceOrientationMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class DiceOrientationMapperTest {
    @Test public void everyTargetRotationPutsRequestedFaceUp() {
        for (int value = 1; value <= 6; value++) {
            assertEquals(value, DiceOrientationMapper.topFaceFor(DiceOrientationMapper.targetAnglesForTop(value)));
        }
    }

    @Test public void standardOppositeFacesAreMapped() {
        assertEquals(6, DiceFaceMapping.oppositeOf(1));
        assertEquals(5, DiceFaceMapping.oppositeOf(2));
        assertEquals(4, DiceFaceMapping.oppositeOf(3));
        assertEquals(3, DiceFaceMapping.oppositeOf(4));
        assertEquals(2, DiceFaceMapping.oppositeOf(5));
        assertEquals(1, DiceFaceMapping.oppositeOf(6));
    }
}
