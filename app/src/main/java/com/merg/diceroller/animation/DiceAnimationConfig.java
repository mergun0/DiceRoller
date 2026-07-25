package com.merg.diceroller.animation;

import com.merg.diceroller.model.AnimationSpeed;

public final class DiceAnimationConfig {
    public final long durationMs;
    public final float lift;
    public final float horizontalDrift;
    public DiceAnimationConfig(AnimationSpeed speed, float horizontalDrift) {
        this.durationMs = (long) (1500L * speed.durationScale);
        this.lift = 0.52f;
        this.horizontalDrift = horizontalDrift;
    }
}
