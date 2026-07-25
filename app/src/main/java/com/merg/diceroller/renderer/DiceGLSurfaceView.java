package com.merg.diceroller.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import com.merg.diceroller.model.AnimationSpeed;
import com.merg.diceroller.model.DiceMode;
import com.merg.diceroller.model.DiceTheme;

public final class DiceGLSurfaceView extends TextureView implements TextureView.SurfaceTextureListener {
    public interface RollFinishedListener { void onRollFinished(DiceMode mode, int first, int second); }

    private final DiceRenderer renderer;
    private final HandlerThread glThread = new HandlerThread("DiceGLThread");
    private final Handler glHandler;
    private RollFinishedListener rollFinishedListener;
    private EGLDisplay eglDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext eglContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private Surface surface;
    private boolean glReady;
    private int surfaceWidth = 1;
    private int surfaceHeight = 1;

    private final Runnable renderLoop = new Runnable() {
        @Override public void run() {
            if (!glReady) return;
            drawFrame();
            if (renderer.isAnimating()) glHandler.postDelayed(this, 16L);
        }
    };

    public DiceGLSurfaceView(Context context) {
        this(context, null);
    }

    public DiceGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOpaque(true);
        glThread.start();
        glHandler = new Handler(glThread.getLooper());
        renderer = new DiceRenderer(context, (mode, first, second) -> post(() -> {
            drawOnce();
            if (rollFinishedListener != null) rollFinishedListener.onRollFinished(mode, first, second);
        }));
        setSurfaceTextureListener(this);
    }

    public void setRollFinishedListener(RollFinishedListener listener) {
        rollFinishedListener = listener;
    }

    public void setDiceTheme(DiceTheme theme) {
        queueEvent(() -> {
            renderer.setTheme(theme);
            drawFrame();
        });
    }

    public void setDiceMode(DiceMode mode) {
        queueEvent(() -> {
            renderer.setMode(mode);
            glHandler.removeCallbacks(renderLoop);
            glHandler.post(renderLoop);
        });
    }

    public void roll(DiceMode mode, int first, int second, AnimationSpeed speed) {
        queueEvent(() -> {
            renderer.roll(mode, first, second, speed);
            glHandler.removeCallbacks(renderLoop);
            glHandler.post(renderLoop);
        });
    }

    public void showValueImmediately(int value) {
        queueEvent(() -> {
            glHandler.removeCallbacks(renderLoop);
            renderer.showValueImmediately(value);
            drawFrame();
        });
    }

    public void stopRoll() {
        queueEvent(() -> {
            renderer.cancel();
            glHandler.removeCallbacks(renderLoop);
            drawFrame();
        });
    }

    public void releaseGl() {
        queueEvent(() -> {
            renderer.release();
            releaseEgl();
            glThread.quitSafely();
        });
    }

    public void onResume() {
        drawOnce();
    }

    public void onPause() {
        stopRoll();
    }

    @Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        surfaceWidth = Math.max(1, width);
        surfaceHeight = Math.max(1, height);
        queueEvent(() -> initEgl(surfaceTexture, surfaceWidth, surfaceHeight));
    }

    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        surfaceWidth = Math.max(1, width);
        surfaceHeight = Math.max(1, height);
        queueEvent(() -> {
            if (glReady) {
                renderer.onSurfaceChanged(null, surfaceWidth, surfaceHeight);
                drawFrame();
            }
        });
    }

    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        queueEvent(this::releaseEgl);
        return true;
    }

    @Override public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    private void queueEvent(Runnable runnable) {
        glHandler.post(runnable);
    }

    private void drawOnce() {
        queueEvent(this::drawFrame);
    }

    private void initEgl(SurfaceTexture surfaceTexture, int width, int height) {
        releaseEgl();
        surface = new Surface(surfaceTexture);
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        EGL14.eglInitialize(eglDisplay, version, 0, version, 1);
        int[] attributes = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] count = new int[1];
        EGL14.eglChooseConfig(eglDisplay, attributes, 0, configs, 0, 1, count, 0);
        int[] contextAttributes = { EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE };
        eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, contextAttributes, 0);
        int[] surfaceAttributes = { EGL14.EGL_NONE };
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], surface, surfaceAttributes, 0);
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        glReady = true;
        renderer.onSurfaceCreated(null, null);
        renderer.onSurfaceChanged(null, width, height);
        drawFrame();
    }

    private void drawFrame() {
        if (!glReady) return;
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        renderer.onDrawFrame(null);
        EGL14.eglSwapBuffers(eglDisplay, eglSurface);
    }

    private void releaseEgl() {
        glHandler.removeCallbacks(renderLoop);
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            if (eglSurface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(eglDisplay, eglSurface);
            if (eglContext != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(eglDisplay, eglContext);
            EGL14.eglTerminate(eglDisplay);
        }
        if (surface != null) {
            surface.release();
            surface = null;
        }
        eglDisplay = EGL14.EGL_NO_DISPLAY;
        eglContext = EGL14.EGL_NO_CONTEXT;
        eglSurface = EGL14.EGL_NO_SURFACE;
        glReady = false;
    }
}
