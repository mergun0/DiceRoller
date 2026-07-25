package com.merg.diceroller.renderer;

import com.merg.diceroller.animation.Quaternion;

public final class DiceOrientationMapper {
    private DiceOrientationMapper() {}

    public static final class TargetAngles {
        public final float x;
        public final float y;
        public final float z;

        public TargetAngles(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static TargetAngles targetAnglesForTop(int value) {
        switch (value) {
            case 1: return new TargetAngles(0f, 0f, 0f);
            case 2: return new TargetAngles(0f, 0f, -90f);
            case 3: return new TargetAngles(-90f, 0f, 0f);
            case 4: return new TargetAngles(90f, 0f, 0f);
            case 5: return new TargetAngles(0f, 0f, 90f);
            case 6: return new TargetAngles(180f, 0f, 0f);
            default: throw new IllegalArgumentException("Dice result must be 1..6");
        }
    }

    public static Quaternion orientationForTop(int value) {
        TargetAngles angles = targetAnglesForTop(value);
        return Quaternion.multiply(Quaternion.fromAxisAngle(1, 0, 0, angles.x),
                Quaternion.multiply(Quaternion.fromAxisAngle(0, 1, 0, angles.y),
                        Quaternion.fromAxisAngle(0, 0, 1, angles.z)));
    }

    public static int topFaceFor(TargetAngles angles) {
        int best = 1;
        float bestY = -999f;
        for (DiceFaceMapping.Face face : DiceFaceMapping.Face.values()) {
            float[] normal = rotateNormal(face.nx, face.ny, face.nz, angles);
            if (normal[1] > bestY) {
                bestY = normal[1];
                best = face.value;
            }
        }
        return best;
    }

    public static int topFaceFor(Quaternion q) {
        int best = 1;
        float bestY = -999f;
        for (DiceFaceMapping.Face face : DiceFaceMapping.Face.values()) {
            float[] n = q.rotate(face.nx, face.ny, face.nz);
            if (n[1] > bestY) {
                bestY = n[1];
                best = face.value;
            }
        }
        return best;
    }

    public static boolean validatesTop(int value) {
        return topFaceFor(targetAnglesForTop(value)) == value;
    }

    public static int oppositeOf(int value) {
        return DiceFaceMapping.oppositeOf(value);
    }

    private static float[] rotateNormal(float x, float y, float z, TargetAngles angles) {
        float[] afterZ = rotateZ(x, y, z, angles.z);
        float[] afterY = rotateY(afterZ[0], afterZ[1], afterZ[2], angles.y);
        return rotateX(afterY[0], afterY[1], afterY[2], angles.x);
    }

    private static float[] rotateX(float x, float y, float z, float degrees) {
        double r = Math.toRadians(degrees);
        float c = (float) Math.cos(r);
        float s = (float) Math.sin(r);
        return new float[] { x, y * c - z * s, y * s + z * c };
    }

    private static float[] rotateY(float x, float y, float z, float degrees) {
        double r = Math.toRadians(degrees);
        float c = (float) Math.cos(r);
        float s = (float) Math.sin(r);
        return new float[] { x * c + z * s, y, -x * s + z * c };
    }

    private static float[] rotateZ(float x, float y, float z, float degrees) {
        double r = Math.toRadians(degrees);
        float c = (float) Math.cos(r);
        float s = (float) Math.sin(r);
        return new float[] { x * c - y * s, x * s + y * c, z };
    }
}
