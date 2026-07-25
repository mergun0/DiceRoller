package com.merg.diceroller;

import com.merg.diceroller.ui.main.RollGate;
import org.junit.Test;
import static org.junit.Assert.*;

public class RollGateTest {
    @Test public void secondRollIsRejectedWhileAnimationIsRunning() {
        RollGate gate = new RollGate();
        assertTrue(gate.tryStart());
        assertFalse(gate.tryStart());
        gate.finish();
        assertTrue(gate.tryStart());
    }
}
