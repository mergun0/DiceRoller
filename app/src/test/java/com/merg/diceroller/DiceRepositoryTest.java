package com.merg.diceroller;

import com.merg.diceroller.model.DiceMode;
import com.merg.diceroller.model.DiceRoll;
import com.merg.diceroller.repository.DiceRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class DiceRepositoryTest {
    @Test public void historyIsLimitedToTwentyRows() {
        List<DiceRoll> rolls = new ArrayList<>();
        for (int i = 0; i < 30; i++) rolls.add(new DiceRoll(DiceMode.SINGLE, 1, 0, i));
        assertEquals(20, DiceRepository.limitToTwenty(rolls).size());
    }

    @Test public void doubleDiceTotalIsCalculated() {
        DiceRoll roll = new DiceRoll(DiceMode.DOUBLE, 3, 5, 1L);
        assertEquals(8, roll.total());
    }
}
