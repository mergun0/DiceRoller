package com.merg.diceroller.renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import com.merg.diceroller.model.AnimationSpeed;
import com.merg.diceroller.model.DiceMode;
import com.merg.diceroller.model.DiceTheme;
import com.merg.diceroller.renderer.DiceOrientationMapper.TargetAngles;
import com.merg.diceroller.renderer.geometry.DiceMesh;
import com.merg.diceroller.renderer.texture.DiceTextureGenerator;
import com.merg.diceroller.util.DiceRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class DiceRenderer implements GLSurfaceView.Renderer {
    public interface Listener { void onRollFinished(DiceMode mode, int firstValue, int secondValue); }

    private static final long ROLL_MS = 1050L;
    private static final long MODE_TRANSITION_MS = 240L;
    private final Context context;
    private final Listener listener;
    private final DiceTextureGenerator textures = new DiceTextureGenerator();
    private final DiceRandom random = new DiceRandom();
    private final AtomicBoolean rolling = new AtomicBoolean(false);
    private final DieState firstDie = new DieState();
    private final DieState secondDie = new DieState();
    private DiceShaderProgram shader;
    private DiceMesh mesh;
    private ShadowRenderer shadow;
    private DiceTheme theme = DiceTheme.all()[0];
    private DiceMode mode = DiceMode.SINGLE;
    private DiceMode transitionFromMode = DiceMode.SINGLE;
    private DiceMode transitionToMode = DiceMode.SINGLE;
    private long modeTransitionStartMs;
    private boolean finishSent;
    private final float[] projection = new float[16];
    private final float[] view = new float[16];
    private final float[] model = new float[16];
    private final float[] modelRotation = new float[16];
    private final float[] modelComposed = new float[16];
    private final float[] mv = new float[16];
    private final float[] mvp = new float[16];
    private final float[] shadowModel = new float[16];
    private final float[] shadowMv = new float[16];
    private final float[] shadowMvp = new float[16];

    public DiceRenderer(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        TargetAngles initial = DiceOrientationMapper.targetAnglesForTop(1);
        firstDie.currentX = initial.x;
        firstDie.currentY = initial.y;
        firstDie.currentZ = initial.z;
        secondDie.currentX = initial.x;
        secondDie.currentY = initial.y + 16f;
        secondDie.currentZ = initial.z - 10f;
    }

    public void setTheme(DiceTheme theme) {
        this.theme = theme;
    }

    public void setMode(DiceMode mode) {
        if (mode == null || rolling.get() || mode == this.mode) return;
        transitionFromMode = this.mode;
        transitionToMode = mode;
        modeTransitionStartMs = System.currentTimeMillis();
        this.mode = mode;
    }

    public void roll(DiceMode mode, int first, int second, AnimationSpeed speed) {
        if (rolling.get()) return;
        if (mode != null) this.mode = mode;
        long now = System.currentTimeMillis();
        configureRoll(firstDie, first, speed, now, false);
        if (this.mode == DiceMode.DOUBLE) {
            configureRoll(secondDie, second, speed, now, true);
        }
        finishSent = false;
        rolling.set(true);
    }

    public void showValueImmediately(int value) {
        TargetAngles target = DiceOrientationMapper.targetAnglesForTop(value);
        firstDie.value = value;
        firstDie.currentX = target.x;
        firstDie.currentY = target.y;
        firstDie.currentZ = target.z;
        firstDie.rolling = false;
        rolling.set(false);
        finishSent = false;
    }

    public boolean isRolling() {
        return rolling.get();
    }

    public boolean isAnimating() {
        return rolling.get() || transitionFromMode != transitionToMode;
    }

    public void cancel() {
        rolling.set(false);
        finishSent = false;
    }

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.965f, 0.975f, 0.992f, 1f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        shader = new DiceShaderProgram();
        mesh = new DiceMesh();
        shadow = new ShadowRenderer();
        textures.setTheme(theme);
        textures.writeDebugBitmaps(context);
    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        GLES20.glViewport(0, 0, safeWidth, safeHeight);
        float aspect = safeWidth / (float) safeHeight;
        Matrix.perspectiveM(projection, 0, 30f, aspect, 1f, 24f);
        Matrix.setLookAtM(view, 0, 2.5f, 5.1f, 4.6f, 0f, 0f, 0f, 0f, 1f, 0f);
    }

    @Override public void onDrawFrame(GL10 gl) {
        textures.setTheme(theme);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        updateAnimation();
        drawShadow();
        drawDie();
    }

    private void updateAnimation() {
        if (!rolling.get()) return;
        long now = System.currentTimeMillis();
        boolean firstRunning = updateDie(firstDie, now);
        boolean secondRunning = mode == DiceMode.DOUBLE && updateDie(secondDie, now);
        if (!firstRunning && !secondRunning) {
            rolling.set(false);
            if (!finishSent) {
                finishSent = true;
                listener.onRollFinished(mode, firstDie.value, mode == DiceMode.DOUBLE ? secondDie.value : 0);
            }
        }
    }

    private void drawDie() {
        float mix = modeMix();
        if (mode == DiceMode.DOUBLE || mix > 0f) {
            float firstX = lerp(0f, -1.18f, mix);
            float secondX = lerp(0f, 1.18f, mix);
            float scale = lerp(1f, 0.64f, mix);
            drawDie(firstDie, firstX, 0f, scale, 1f);
            drawDie(secondDie, secondX, 0.02f, scale * mix, mix);
        } else {
            drawDie(firstDie, 0f, 0f, 1f, 1f);
        }
    }

    private void drawDie(DieState die, float x, float y, float scale, float alpha) {
        if (scale <= 0.02f || alpha <= 0.02f) return;
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x + die.xOffset, y + die.yOffset, 0f);
        Matrix.scaleM(model, 0, scale, scale, scale);
        Matrix.setIdentityM(modelRotation, 0);
        Matrix.rotateM(modelRotation, 0, die.currentX, 1f, 0f, 0f);
        Matrix.rotateM(modelRotation, 0, die.currentY, 0f, 1f, 0f);
        Matrix.rotateM(modelRotation, 0, die.currentZ, 0f, 0f, 1f);
        Matrix.multiplyMM(modelComposed, 0, model, 0, modelRotation, 0);
        System.arraycopy(modelComposed, 0, model, 0, 16);
        Matrix.multiplyMM(mv, 0, view, 0, model, 0);
        Matrix.multiplyMM(mvp, 0, projection, 0, mv, 0);
        GLES20.glUseProgram(shader.program);
        GLES20.glUniformMatrix4fv(shader.uMVP, 1, false, mvp, 0);
        GLES20.glUniformMatrix4fv(shader.uModel, 1, false, model, 0);
        GLES20.glUniformMatrix4fv(shader.uNormalMatrix, 1, false, model, 0);
        GLES20.glUniform3f(shader.uLightDir, -0.45f, -0.82f, -0.38f);
        GLES20.glUniform3f(shader.uViewPos, 2.5f, 5.1f, 4.6f);
        GLES20.glUniform1f(shader.uAmbient, 0.64f);
        GLES20.glUniform1f(shader.uDiffuse, 0.44f);
        GLES20.glUniform1f(shader.uSpecular, 0.08f);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(shader.uTexture, 0);
        mesh.draw(shader, faceValue -> GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures.textureFor(faceValue)));
    }

    private void drawShadow() {
        float mix = modeMix();
        if (mode == DiceMode.DOUBLE || mix > 0f) {
            float firstX = lerp(0f, -1.18f, mix);
            float secondX = lerp(0f, 1.18f, mix);
            float scale = lerp(1f, 0.64f, mix);
            drawShadow(firstDie, firstX, scale, 0.20f);
            drawShadow(secondDie, secondX, scale * mix, 0.20f * mix);
        } else {
            drawShadow(firstDie, 0f, 1f, 0.20f);
        }
    }

    private void drawShadow(DieState die, float x, float scale, float alpha) {
        if (scale <= 0.02f || alpha <= 0.02f) return;
        Matrix.setIdentityM(shadowModel, 0);
        Matrix.translateM(shadowModel, 0, x + die.xOffset, -1.08f, 0f);
        Matrix.scaleM(shadowModel, 0, 1.15f * scale, 1f, 0.78f * scale);
        Matrix.multiplyMM(shadowMv, 0, view, 0, shadowModel, 0);
        Matrix.multiplyMM(shadowMvp, 0, projection, 0, shadowMv, 0);
        GLES20.glDepthMask(false);
        shadow.draw(shadowMvp, alpha);
        GLES20.glDepthMask(true);
    }

    public void release() {
        textures.release();
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    private static float easeOutCubic(float t) {
        float p = 1f - t;
        return 1f - p * p * p;
    }

    private void configureRoll(DieState die, int value, AnimationSpeed speed, long now, boolean secondary) {
        TargetAngles target = DiceOrientationMapper.targetAnglesForTop(value);
        die.value = value;
        die.startX = die.currentX;
        die.startY = die.currentY;
        die.startZ = die.currentZ;
        float variant = secondary ? 1f : -1f;
        die.endX = target.x + 360f * random.nextTurns(2, 3) + random.nextSignedFloat(24f);
        die.endY = target.y + 360f * random.nextTurns(3, 4) + variant * 18f;
        die.endZ = target.z + 360f * random.nextTurns(1, 3) - variant * 14f;
        die.startMs = now;
        float durationScale = speed == null ? 1f : speed.durationScale;
        die.durationMs = Math.round((ROLL_MS + (secondary ? 70L : 0L)) * durationScale);
        die.drift = secondary ? -0.045f : 0.045f;
        die.rolling = true;
    }

    private boolean updateDie(DieState die, long now) {
        if (!die.rolling) return false;
        float t = Math.min(1f, (now - die.startMs) / (float) die.durationMs);
        float eased = easeOutCubic(t);
        die.currentX = lerp(die.startX, die.endX, eased);
        die.currentY = lerp(die.startY, die.endY, eased);
        die.currentZ = lerp(die.startZ, die.endZ, eased);
        die.xOffset = die.drift * (float) Math.sin(Math.PI * t);
        die.yOffset = 0.045f * (float) Math.sin(Math.PI * Math.min(1f, t * 1.08f));
        if (t >= 1f) {
            TargetAngles exact = DiceOrientationMapper.targetAnglesForTop(die.value);
            die.currentX = exact.x;
            die.currentY = exact.y;
            die.currentZ = exact.z;
            die.xOffset = 0f;
            die.yOffset = 0f;
            die.rolling = false;
            return false;
        }
        return true;
    }

    private float modeMix() {
        if (transitionFromMode == transitionToMode) return mode == DiceMode.DOUBLE ? 1f : 0f;
        float t = Math.min(1f, (System.currentTimeMillis() - modeTransitionStartMs) / (float) MODE_TRANSITION_MS);
        float eased = easeOutCubic(t);
        if (t >= 1f) transitionFromMode = transitionToMode;
        return transitionToMode == DiceMode.DOUBLE ? eased : 1f - eased;
    }

    private static final class DieState {
        int value = 1;
        long startMs;
        long durationMs = ROLL_MS;
        float startX, startY, startZ;
        float currentX, currentY, currentZ;
        float endX, endY, endZ;
        float xOffset, yOffset;
        float drift;
        boolean rolling;
    }
}
