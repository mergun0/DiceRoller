package com.merg.diceroller.viewmodel;

import com.merg.diceroller.model.AnimationSpeed;
import com.merg.diceroller.model.AppTheme;
import com.merg.diceroller.model.DiceMode;
import com.merg.diceroller.model.DiceRoll;
import com.merg.diceroller.model.DiceTheme;
import java.util.List;

public final class MainUiState {
    public final DiceMode diceMode;
    public final int firstDiceValue;
    public final int secondDiceValue;
    public final int total;
    public final boolean isRolling;
    public final DiceTheme selectedDiceTheme;
    public final boolean soundEnabled;
    public final boolean vibrationEnabled;
    public final boolean shakeEnabled;
    public final AppTheme appTheme;
    public final AnimationSpeed animationSpeed;
    public final List<DiceRoll> recentRolls;

    public MainUiState(DiceMode diceMode, int firstDiceValue, int secondDiceValue, boolean isRolling,
                       DiceTheme selectedDiceTheme, boolean soundEnabled, boolean vibrationEnabled,
                       boolean shakeEnabled, AppTheme appTheme, AnimationSpeed animationSpeed, List<DiceRoll> recentRolls) {
        this.diceMode = diceMode;
        this.firstDiceValue = firstDiceValue;
        this.secondDiceValue = secondDiceValue;
        this.total = diceMode == DiceMode.DOUBLE ? firstDiceValue + secondDiceValue : firstDiceValue;
        this.isRolling = isRolling;
        this.selectedDiceTheme = selectedDiceTheme;
        this.soundEnabled = soundEnabled;
        this.vibrationEnabled = vibrationEnabled;
        this.shakeEnabled = shakeEnabled;
        this.appTheme = appTheme;
        this.animationSpeed = animationSpeed;
        this.recentRolls = recentRolls;
    }

    public int total() { return total; }
}
