package com.merg.diceroller;

import com.merg.diceroller.util.DiceRandom;
import org.junit.Test;
import static org.junit.Assert.*;

public class DiceRandomTest {
    @Test public void secureRandomResultIsInDiceRange() {
        DiceRandom random = new DiceRandom();
        for (int i = 0; i < 1000; i++) {
            int value = random.nextDie();
            assertTrue(value >= 1 && value <= 6);
        }
    }
}
