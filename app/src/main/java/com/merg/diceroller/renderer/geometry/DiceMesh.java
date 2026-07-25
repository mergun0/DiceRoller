package com.merg.diceroller.renderer.geometry;

import android.opengl.GLES20;
import com.merg.diceroller.renderer.DiceFaceMapping;
import com.merg.diceroller.renderer.DiceShaderProgram;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public final class DiceMesh {
    private static final int STRIDE = 8 * 4;
    private final FloatBuffer vertices;
    private final ShortBuffer indices;
    private final int[] faceIndexStarts = new int[DiceFaceMapping.Face.values().length];

    public DiceMesh() {
        float[] vertexData = buildVertices(1f);
        short[] indexData = buildIndices();
        vertices = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(vertexData).position(0);
        indices = ByteBuffer.allocateDirect(indexData.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        indices.put(indexData).position(0);
    }

    public void draw(DiceShaderProgram shader, TextureBinder binder) {
        vertices.position(0);
        GLES20.glVertexAttribPointer(shader.aPosition, 3, GLES20.GL_FLOAT, false, STRIDE, vertices);
        GLES20.glEnableVertexAttribArray(shader.aPosition);
        vertices.position(3);
        GLES20.glVertexAttribPointer(shader.aNormal, 3, GLES20.GL_FLOAT, false, STRIDE, vertices);
        GLES20.glEnableVertexAttribArray(shader.aNormal);
        vertices.position(6);
        GLES20.glVertexAttribPointer(shader.aTexCoord, 2, GLES20.GL_FLOAT, false, STRIDE, vertices);
        GLES20.glEnableVertexAttribArray(shader.aTexCoord);

        DiceFaceMapping.Face[] faces = DiceFaceMapping.Face.values();
        for (int faceIndex = 0; faceIndex < faces.length; faceIndex++) {
            binder.bind(faces[faceIndex].value);
            indices.position(faceIndexStarts[faceIndex]);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indices);
        }
    }

    public interface TextureBinder { void bind(int faceValue); }

    private float[] buildVertices(float s) {
        float[] uv = {0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f};
        float[][][] corners = {
                {{-s, s, -s}, {s, s, -s}, {s, s, s}, {-s, s, s}},
                {{-s, -s, s}, {s, -s, s}, {s, -s, -s}, {-s, -s, -s}},
                {{-s, -s, s}, {s, -s, s}, {s, s, s}, {-s, s, s}},
                {{s, -s, -s}, {-s, -s, -s}, {-s, s, -s}, {s, s, -s}},
                {{-s, -s, -s}, {-s, -s, s}, {-s, s, s}, {-s, s, -s}},
                {{s, -s, s}, {s, -s, -s}, {s, s, -s}, {s, s, s}}
        };
        DiceFaceMapping.Face[] faces = DiceFaceMapping.Face.values();
        float[] out = new float[faces.length * 4 * 8];
        int p = 0;
        for (int faceIndex = 0; faceIndex < faces.length; faceIndex++) {
            DiceFaceMapping.Face face = faces[faceIndex];
            for (int v = 0; v < 4; v++) {
                out[p++] = corners[faceIndex][v][0];
                out[p++] = corners[faceIndex][v][1];
                out[p++] = corners[faceIndex][v][2];
                out[p++] = face.nx;
                out[p++] = face.ny;
                out[p++] = face.nz;
                out[p++] = uv[v * 2];
                out[p++] = uv[v * 2 + 1];
            }
        }
        return out;
    }

    private short[] buildIndices() {
        short[] out = new short[DiceFaceMapping.Face.values().length * 6];
        int p = 0;
        for (int face = 0; face < DiceFaceMapping.Face.values().length; face++) {
            faceIndexStarts[face] = p;
            short base = (short) (face * 4);
            out[p++] = base;
            out[p++] = (short) (base + 1);
            out[p++] = (short) (base + 2);
            out[p++] = base;
            out[p++] = (short) (base + 2);
            out[p++] = (short) (base + 3);
        }
        return out;
    }
}
