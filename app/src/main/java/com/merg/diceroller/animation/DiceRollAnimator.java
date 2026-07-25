package com.merg.diceroller.animation;

import com.merg.diceroller.renderer.DiceOrientationMapper;
import com.merg.diceroller.util.DiceRandom;

public final class DiceRollAnimator {
    private final DiceRandom random = new DiceRandom();
    private final DiceAnimationState state = new DiceAnimationState();
    private Quaternion start = new Quaternion();
    private Quaternion target = new Quaternion();
    private long startTime;
    private DiceAnimationConfig config;
    private boolean completed;

    public DiceAnimationState state() { return state; }

    public void start(int value, DiceAnimationConfig config, long nowMs) {
        this.config = config;
        start = state.orientation.copy();
        Quaternion finalPose = DiceOrientationMapper.orientationForTop(value);
        Quaternion spin = Quaternion.multiply(Quaternion.fromAxisAngle(1, 0, 0, 360f * random.nextTurns(2, 4)),
                Quaternion.multiply(Quaternion.fromAxisAngle(0, 1, 0, 360f * random.nextTurns(2, 5)),
                        Quaternion.fromAxisAngle(0, 0, 1, 360f * random.nextTurns(1, 4))));
        target = Quaternion.multiply(spin, finalPose);
        startTime = nowMs;
        completed = false;
        state.running = true;
    }

    public boolean update(long nowMs) {
        if (!state.running || config == null) return false;
        float t = Math.min(1f, (nowMs - startTime) / (float) config.durationMs);
        float eased = easeOutCubic(t);
        Quaternion visual = Quaternion.slerp(start, target, eased);
        state.orientation.set(visual.x, visual.y, visual.z, visual.w);
        float liftCurve = (float) Math.sin(Math.PI * Math.min(1f, t * 1.15f));
        float bounce = t > 0.76f ? (float) Math.sin((t - 0.76f) / 0.24f * Math.PI * 2f) * (1f - t) * 0.16f : 0f;
        state.yOffset = Math.max(0f, liftCurve * config.lift + bounce);
        state.xOffset = config.horizontalDrift * (float) Math.sin(Math.PI * t);
        state.shadowScale = 1.05f - state.yOffset * 0.38f;
        state.shadowAlpha = 0.42f - state.yOffset * 0.24f;
        if (t >= 1f) {
            Quaternion finalPose = DiceOrientationMapper.orientationForTop(DiceOrientationMapper.topFaceFor(target));
            state.orientation.set(finalPose.x, finalPose.y, finalPose.z, finalPose.w);
            state.yOffset = 0f;
            state.shadowScale = 1.05f;
            state.shadowAlpha = 0.42f;
            state.running = false;
            completed = true;
        }
        return true;
    }

    public boolean consumeCompleted() {
        if (!completed) return false;
        completed = false;
        return true;
    }

    public void cancel() { state.running = false; completed = false; }

    private static float easeOutCubic(float t) {
        float p = 1f - t;
        return 1f - p * p * p;
    }
}
