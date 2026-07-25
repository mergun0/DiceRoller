package com.merg.diceroller.viewmodel;

import com.merg.diceroller.model.AnimationSpeed;
import com.merg.diceroller.model.DiceMode;

public final class RollCommand {
    public final DiceMode mode;
    public final int first;
    public final int second;
    public final AnimationSpeed speed;
    public RollCommand(DiceMode mode, int first, int second, AnimationSpeed speed) {
        this.mode = mode;
        this.first = first;
        this.second = second;
        this.speed = speed;
    }
}
