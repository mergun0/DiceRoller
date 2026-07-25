package com.merg.diceroller.util;

import java.security.SecureRandom;

public final class DiceRandom {
    private final SecureRandom secureRandom;
    public DiceRandom() { this(new SecureRandom()); }
    public DiceRandom(SecureRandom secureRandom) { this.secureRandom = secureRandom; }
    public int nextDie() { return secureRandom.nextInt(6) + 1; }
    public int nextTurns(int minInclusive, int maxInclusive) {
        return minInclusive + secureRandom.nextInt(maxInclusive - minInclusive + 1);
    }
    public float nextSignedFloat(float magnitude) {
        return (secureRandom.nextFloat() * 2f - 1f) * magnitude;
    }
}
