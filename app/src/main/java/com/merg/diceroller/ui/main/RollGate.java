package com.merg.diceroller.ui.main;

public final class RollGate {
    private boolean rolling;

    public boolean tryStart() {
        if (rolling) return false;
        rolling = true;
        return true;
    }

    public void finish() {
        rolling = false;
    }

    public boolean isRolling() {
        return rolling;
    }
}
