package com.merg.diceroller.renderer;

public final class DiceFaceMapping {
    private DiceFaceMapping() {}

    public enum Face {
        TOP(1, 0f, 1f, 0f),
        BOTTOM(6, 0f, -1f, 0f),
        FRONT(3, 0f, 0f, 1f),
        BACK(4, 0f, 0f, -1f),
        LEFT(2, -1f, 0f, 0f),
        RIGHT(5, 1f, 0f, 0f);

        public final int value;
        public final float nx;
        public final float ny;
        public final float nz;

        Face(int value, float nx, float ny, float nz) {
            this.value = value;
            this.nx = nx;
            this.ny = ny;
            this.nz = nz;
        }
    }

    public static Face faceForValue(int value) {
        for (Face face : Face.values()) if (face.value == value) return face;
        throw new IllegalArgumentException("Dice result must be 1..6");
    }

    public static int oppositeOf(int value) {
        switch (value) {
            case 1: return 6;
            case 6: return 1;
            case 2: return 5;
            case 5: return 2;
            case 3: return 4;
            case 4: return 3;
            default: throw new IllegalArgumentException("Dice result must be 1..6");
        }
    }
}
