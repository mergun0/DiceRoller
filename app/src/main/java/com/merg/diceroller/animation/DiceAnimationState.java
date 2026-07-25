package com.merg.diceroller.animation;

public final class DiceAnimationState {
    public final Quaternion orientation = new Quaternion();
    public float yOffset;
    public float xOffset;
    public float shadowScale = 1f;
    public float shadowAlpha = 0.38f;
    public boolean running;
}
