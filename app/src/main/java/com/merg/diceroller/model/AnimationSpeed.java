package com.merg.diceroller.model;

public enum AnimationSpeed {
    NORMAL(1.0f), FAST(0.72f);
    public final float durationScale;
    AnimationSpeed(float durationScale) { this.durationScale = durationScale; }
}
