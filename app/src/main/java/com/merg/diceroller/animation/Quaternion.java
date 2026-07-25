package com.merg.diceroller.animation;

public final class Quaternion {
    public float x, y, z, w;

    public Quaternion() { set(0, 0, 0, 1); }
    public Quaternion(float x, float y, float z, float w) { set(x, y, z, w); }
    public Quaternion set(float x, float y, float z, float w) { this.x = x; this.y = y; this.z = z; this.w = w; return normalize(); }
    public Quaternion copy() { return new Quaternion(x, y, z, w); }

    public Quaternion normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z + w * w);
        if (length == 0f) return set(0, 0, 0, 1);
        x /= length; y /= length; z /= length; w /= length;
        return this;
    }

    public static Quaternion fromAxisAngle(float ax, float ay, float az, float degrees) {
        float radians = (float) Math.toRadians(degrees) * 0.5f;
        float sin = (float) Math.sin(radians);
        return new Quaternion(ax * sin, ay * sin, az * sin, (float) Math.cos(radians));
    }

    public static Quaternion multiply(Quaternion a, Quaternion b) {
        return new Quaternion(
                a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y,
                a.w * b.y - a.x * b.z + a.y * b.w + a.z * b.x,
                a.w * b.z + a.x * b.y - a.y * b.x + a.z * b.w,
                a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z);
    }

    public static Quaternion slerp(Quaternion from, Quaternion to, float t) {
        float dot = from.x * to.x + from.y * to.y + from.z * to.z + from.w * to.w;
        Quaternion end = to;
        if (dot < 0f) { dot = -dot; end = new Quaternion(-to.x, -to.y, -to.z, -to.w); }
        if (dot > 0.9995f) {
            return new Quaternion(from.x + t * (end.x - from.x), from.y + t * (end.y - from.y),
                    from.z + t * (end.z - from.z), from.w + t * (end.w - from.w));
        }
        float theta0 = (float) Math.acos(dot);
        float theta = theta0 * t;
        float sinTheta = (float) Math.sin(theta);
        float sinTheta0 = (float) Math.sin(theta0);
        float s0 = (float) Math.cos(theta) - dot * sinTheta / sinTheta0;
        float s1 = sinTheta / sinTheta0;
        return new Quaternion(s0 * from.x + s1 * end.x, s0 * from.y + s1 * end.y,
                s0 * from.z + s1 * end.z, s0 * from.w + s1 * end.w);
    }

    public void toMatrix(float[] matrix) {
        float xx = x * x, yy = y * y, zz = z * z;
        float xy = x * y, xz = x * z, yz = y * z;
        float wx = w * x, wy = w * y, wz = w * z;
        matrix[0] = 1f - 2f * (yy + zz); matrix[1] = 2f * (xy + wz); matrix[2] = 2f * (xz - wy); matrix[3] = 0f;
        matrix[4] = 2f * (xy - wz); matrix[5] = 1f - 2f * (xx + zz); matrix[6] = 2f * (yz + wx); matrix[7] = 0f;
        matrix[8] = 2f * (xz + wy); matrix[9] = 2f * (yz - wx); matrix[10] = 1f - 2f * (xx + yy); matrix[11] = 0f;
        matrix[12] = 0f; matrix[13] = 0f; matrix[14] = 0f; matrix[15] = 1f;
    }

    public float[] rotate(float vx, float vy, float vz) {
        float[] m = new float[16];
        toMatrix(m);
        return new float[] {
                m[0] * vx + m[4] * vy + m[8] * vz,
                m[1] * vx + m[5] * vy + m[9] * vz,
                m[2] * vx + m[6] * vy + m[10] * vz
        };
    }
}
