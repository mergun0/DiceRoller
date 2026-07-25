package com.merg.diceroller.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public final class ShakeDetector implements SensorEventListener {
    public interface Listener { void onShake(); }
    private static final float THRESHOLD = 15.5f;
    private static final long COOLDOWN_MS = 1200L;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Listener listener;
    private long lastShake;
    private boolean enabled;

    public ShakeDetector(Context context, Listener listener) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.listener = listener;
    }

    public boolean isAvailable() { return accelerometer != null; }

    public void start(boolean enabled) {
        this.enabled = enabled && isAvailable();
        if (this.enabled) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        enabled = false;
    }

    @Override public void onSensorChanged(SensorEvent event) {
        if (!enabled) return;
        float x = event.values[0], y = event.values[1], z = event.values[2];
        float force = (float) Math.sqrt(x * x + y * y + z * z);
        long now = System.currentTimeMillis();
        if (force > THRESHOLD && now - lastShake > COOLDOWN_MS) {
            lastShake = now;
            listener.onShake();
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public boolean canTrigger(long nowMs) {
        if (nowMs - lastShake <= COOLDOWN_MS) return false;
        lastShake = nowMs;
        return true;
    }

    public static boolean cooldownAllows(long lastShakeMs, long nowMs) {
        return nowMs - lastShakeMs > COOLDOWN_MS;
    }
}
