package com.merg.diceroller.renderer.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.merg.diceroller.model.DiceTheme;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DiceTextureGenerator {
    public static final int SIZE = 512;
    private static final int FACE_COLOR = 0xFFFAFAF7;
    private static final int PIP_COLOR = Color.BLACK;
    private final Map<Integer, Bitmap> bitmapCache = new HashMap<>();
    private final int[] textureIds = new int[7];
    private DiceTheme theme;

    public void setTheme(DiceTheme theme) {
        if (this.theme != null && this.theme.themeId.equals(theme.themeId)) return;
        this.theme = theme;
        clearBitmaps();
        deleteTextures();
    }

    public int textureFor(int value) {
        if (textureIds[value] != 0) return textureIds[value];
        int[] id = new int[1];
        GLES20.glGenTextures(1, id, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapFor(value), 0);
        textureIds[value] = id[0];
        return id[0];
    }

    public Bitmap bitmapFor(int value) {
        Bitmap cached = bitmapCache.get(value);
        if (cached != null) return cached;
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint facePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        facePaint.setShader(new LinearGradient(0f, 0f, SIZE, SIZE, 0xFFFFFFFF, FACE_COLOR, Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 0f, SIZE, SIZE, facePaint);
        Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(SIZE * 0.025f);
        edgePaint.setColor(0xFFD9DEE8);
        canvas.drawRect(edgePaint.getStrokeWidth() * 0.5f, edgePaint.getStrokeWidth() * 0.5f,
                SIZE - edgePaint.getStrokeWidth() * 0.5f, SIZE - edgePaint.getStrokeWidth() * 0.5f, edgePaint);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(PIP_COLOR);
        float radius = SIZE * 0.075f;
        for (float[] center : pipCenters(value)) canvas.drawCircle(center[0], center[1], radius, paint);
        bitmapCache.put(value, bitmap);
        return bitmap;
    }

    public static List<float[]> pipCenters(int value) {
        float left = SIZE * 0.25f;
        float middle = SIZE * 0.50f;
        float right = SIZE * 0.75f;
        float top = SIZE * 0.25f;
        float center = SIZE * 0.50f;
        float bottom = SIZE * 0.75f;
        ArrayList<float[]> points = new ArrayList<>();
        switch (value) {
            case 1:
                points.add(point(middle, center));
                break;
            case 2:
                points.add(point(left, top));
                points.add(point(right, bottom));
                break;
            case 3:
                points.add(point(left, top));
                points.add(point(middle, center));
                points.add(point(right, bottom));
                break;
            case 4:
                points.add(point(left, top));
                points.add(point(right, top));
                points.add(point(left, bottom));
                points.add(point(right, bottom));
                break;
            case 5:
                points.add(point(left, top));
                points.add(point(right, top));
                points.add(point(middle, center));
                points.add(point(left, bottom));
                points.add(point(right, bottom));
                break;
            case 6:
                points.add(point(left, top));
                points.add(point(left, center));
                points.add(point(left, bottom));
                points.add(point(right, top));
                points.add(point(right, center));
                points.add(point(right, bottom));
                break;
            default:
                throw new IllegalArgumentException("Dice result must be 1..6");
        }
        return points;
    }

    public void writeDebugBitmaps(Context context) {
        if ((context.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) == 0) return;
        File dir = new File(context.getCacheDir(), "dice_faces");
        if (!dir.exists() && !dir.mkdirs()) return;
        for (int value = 1; value <= 6; value++) {
            File out = new File(dir, "face_" + value + ".png");
            try (FileOutputStream stream = new FileOutputStream(out)) {
                bitmapFor(value).compress(Bitmap.CompressFormat.PNG, 100, stream);
            } catch (IOException ignored) {
            }
        }
    }

    public void release() {
        clearBitmaps();
        deleteTextures();
    }

    private static float[] point(float x, float y) {
        return new float[] { x, y };
    }

    private void clearBitmaps() {
        for (Bitmap bitmap : bitmapCache.values()) bitmap.recycle();
        bitmapCache.clear();
    }

    private void deleteTextures() {
        for (int i = 1; i < textureIds.length; i++) {
            if (textureIds[i] != 0) {
                int[] id = { textureIds[i] };
                GLES20.glDeleteTextures(1, id, 0);
                textureIds[i] = 0;
            }
        }
    }
}
