package com.merg.diceroller.model;

import androidx.annotation.StringRes;
import com.merg.diceroller.R;

public final class DiceTheme {
    public final String themeId;
    @StringRes public final int displayNameResource;
    public final int faceColor;
    public final int pipColor;
    public final int edgeColor;
    public final float cornerRadius;
    public final float pipRadius;
    public final float ambientLight;
    public final float diffuseStrength;
    public final float specularStrength;

    public DiceTheme(String themeId, int displayNameResource, int faceColor, int pipColor, int edgeColor,
                     float cornerRadius, float pipRadius, float ambientLight, float diffuseStrength, float specularStrength) {
        this.themeId = themeId;
        this.displayNameResource = displayNameResource;
        this.faceColor = faceColor;
        this.pipColor = pipColor;
        this.edgeColor = edgeColor;
        this.cornerRadius = cornerRadius;
        this.pipRadius = pipRadius;
        this.ambientLight = ambientLight;
        this.diffuseStrength = diffuseStrength;
        this.specularStrength = specularStrength;
    }

    public static DiceTheme[] all() {
        return new DiceTheme[] {
                new DiceTheme("classic", R.string.dice_theme_classic, 0xFFF8F5EF, 0xFF151515, 0xFFD7D0C3, 0.12f, 0.105f, 0.34f, 0.72f, 0.22f),
                new DiceTheme("night", R.string.dice_theme_night, 0xFF22242A, 0xFFF4F4F0, 0xFF3A3D45, 0.12f, 0.105f, 0.28f, 0.68f, 0.18f),
                new DiceTheme("red", R.string.dice_theme_red, 0xFF8F1D22, 0xFFFFF8EE, 0xFF681419, 0.12f, 0.105f, 0.30f, 0.72f, 0.20f),
                new DiceTheme("navy_gold", R.string.dice_theme_navy_gold, 0xFF102747, 0xFFE8C766, 0xFF0B1A30, 0.12f, 0.105f, 0.30f, 0.70f, 0.24f)
        };
    }

    public static DiceTheme byId(String id) {
        for (DiceTheme theme : all()) if (theme.themeId.equals(id)) return theme;
        return all()[0];
    }
}
